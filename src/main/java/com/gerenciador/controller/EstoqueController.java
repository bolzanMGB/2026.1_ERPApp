package com.gerenciador.controller;

import com.gerenciador.app.DadosRepositorio;
import com.gerenciador.model.comercializavel.MateriaPrima;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Modality;
import javafx.util.Callback;
import javafx.util.Duration;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class EstoqueController implements Initializable {

    @FXML private AnchorPane pane;
    @FXML private Label h1Formulario;
    @FXML private Label tipoFormularioLabel;
    @FXML private HBox boxCadastrar;
    @FXML private HBox boxAtualizar;
    @FXML private TextField txtNome;
    @FXML private TextField txtUnidade;
    @FXML private TextField txtPesquisa;
    @FXML private Label erroLabel;
    @FXML private Label sucessoLabel;
    @FXML private StackPane containerMensagens;
    @FXML private TableView<MateriaPrima> tabelaEstoque;
    @FXML private TableColumn<MateriaPrima, Integer> colId;
    @FXML private TableColumn<MateriaPrima, String> colNome, colUnidade;
    @FXML private TableColumn<MateriaPrima, BigDecimal> colEstoqueAtual, colTotalComprado, colTotalVendido;
    @FXML private TableColumn<MateriaPrima, Void> colAcoes;
    private FilteredList<MateriaPrima> listaFiltrada;
    private MateriaPrima materiaPrimaEmEdicao = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTabela();
        configurarPesquisa();
        configurarLimitadores();
        configurarAtalhosTeclado();
        configurarCss();
    }
    public void configurarCss(){
        URL cssFC = getClass().getResource("/com/gerenciador/css/formularios.css");
        URL cssTC = getClass().getResource("/com/gerenciador/css/tabelas.css");
        if (cssFC != null) pane.getStylesheets().add(cssFC.toExternalForm());
        if (cssTC != null) pane.getStylesheets().add(cssTC.toExternalForm());
    }
    private void configurarAtalhosTeclado() {
        pane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    cadastrar();
                    break;
                case ESCAPE:
                    if (materiaPrimaEmEdicao != null) ativarModoCadastro();
                    break;
                default:
                    break;
            }
        });
    }

    private void configurarLimitadores() {
        limitarCaracteres(txtNome, 60);
        limitarCaracteres(txtUnidade, 20);
        forcarPrimeiraLetraMaiuscula(txtNome);
        forcarPrimeiraLetraMaiuscula(txtUnidade);
    }

    private void limitarCaracteres(TextField field, int limite) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > limite) {
                field.setText(oldValue);
            }
        });
    }

    private void configurarPesquisa() {
        txtPesquisa.textProperty().addListener((observable, oldValue, newValue) -> {
            atualizarFiltros();
        });
    }

    @FXML
    public void atualizarFiltros() {
        String termoBusca = txtPesquisa.getText().toLowerCase().trim();

        listaFiltrada.setPredicate(materia -> {
            if (termoBusca.isEmpty()) {
                return true;
            }

            return materia.getNome().toLowerCase().contains(termoBusca) ||
                    materia.getUnidade().toLowerCase().contains(termoBusca);
        });
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colUnidade.setCellValueFactory(new PropertyValueFactory<>("unidade"));
        colEstoqueAtual.setCellValueFactory(new PropertyValueFactory<>("estoqueAtual"));
        colTotalComprado.setCellValueFactory(new PropertyValueFactory<>("totalComprado"));
        colTotalVendido.setCellValueFactory(new PropertyValueFactory<>("totalVendido"));

        colAcoes.setCellFactory(criarColunaAcoes());
        listaFiltrada = new FilteredList<>(DadosRepositorio.getMateriasPrimas(), p -> true);
        SortedList<MateriaPrima> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tabelaEstoque.comparatorProperty());
        tabelaEstoque.setItems(sortedData);
        tabelaEstoque.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
    }

    private Callback<TableColumn<MateriaPrima, Void>, TableCell<MateriaPrima, Void>> criarColunaAcoes() {
        return new Callback<>() {
            @Override
            public TableCell<MateriaPrima, Void> call(TableColumn<MateriaPrima, Void> param) {
                return new TableCell<>() {
                    private final Button btnEdit = new Button();
                    private final Button btnDelete = new Button();
                    private final HBox container = new HBox(10, btnEdit, btnDelete);

                    {
                        ImageView editIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/gerenciador/icons/edit.png"))));
                        ImageView deleteIcon = new ImageView(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/com/gerenciador/icons/delete.png"))));

                        editIcon.setFitWidth(15); editIcon.setFitHeight(15);
                        deleteIcon.setFitWidth(15); deleteIcon.setFitHeight(15);

                        btnEdit.setGraphic(editIcon);
                        btnDelete.setGraphic(deleteIcon);
                        container.setAlignment(Pos.CENTER);
                        btnEdit.getStyleClass().add("btn-edit");
                        btnDelete.getStyleClass().add("btn-delete");

                        btnDelete.setOnAction(e -> removerMateriaPrima(getTableView().getItems().get(getIndex())));
                        btnEdit.setOnAction(e -> editarMateriaPrima(getTableView().getItems().get(getIndex())));
                    }

                    @Override
                    protected void updateItem(Void item, boolean empty) {
                        super.updateItem(item, empty);
                        setGraphic(empty ? null : container);
                    }
                };
            }
        };
    }

    private void removerMateriaPrima(MateriaPrima mp) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Exclusão");
        alert.setHeaderText("Remover Matéria-Prima");
        alert.setContentText("Deseja mesmo remover o item " + mp.getNome() + "?\nIsso pode afetar o histórico de transações.");

        if (pane.getScene() != null) alert.initModality(Modality.APPLICATION_MODAL);

        ButtonType btnSim = new ButtonType("Sim, Remover", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnCancelar, btnSim);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnSim) {
            DadosRepositorio.removerMateriaPrima(mp);
            tabelaEstoque.getItems().remove(mp);
            mostrarSucesso("Item removido");
        }
    }

    private void editarMateriaPrima(MateriaPrima mp) {
        ativarModoEdicao(mp);
        txtNome.setText(mp.getNome());
        txtUnidade.setText(mp.getUnidade());
    }

    @FXML
    public void ativarModoCadastro() {
        h1Formulario.setText("Cadastrar Matéria-Prima");
        tipoFormularioLabel.setText("Nova Matéria-Prima");
        materiaPrimaEmEdicao = null;
        boxCadastrar.setVisible(true); boxCadastrar.setManaged(true);
        boxAtualizar.setVisible(false); boxAtualizar.setManaged(false);
        limparBordas();
        limparInputs();
    }

    private void ativarModoEdicao(MateriaPrima mp) {
        h1Formulario.setText("Editar Matéria-Prima");
        tipoFormularioLabel.setText(mp.getNome());
        materiaPrimaEmEdicao = mp;
        boxCadastrar.setVisible(false); boxCadastrar.setManaged(false);
        boxAtualizar.setVisible(true); boxAtualizar.setManaged(true);
        limparBordas();
    }

    @FXML
    public void cadastrar() {
        try {
            limparBordas();

            String nome = txtNome.getText();
            String unidade = txtUnidade.getText();

            if (materiaPrimaEmEdicao == null) {
                MateriaPrima novaMp = new MateriaPrima(nome, unidade);
                DadosRepositorio.adicionarMateriaPrima(novaMp);
                limparInputs();
                mostrarSucesso("Matéria-Prima cadastrada");
            } else {
                materiaPrimaEmEdicao.setNome(nome);
                materiaPrimaEmEdicao.setUnidade(unidade);
                DadosRepositorio.atualizarMateriaPrima(materiaPrimaEmEdicao);
                tabelaEstoque.refresh();
                ativarModoCadastro();
                mostrarSucesso("Atualização realizada");
            }
        } catch (IllegalArgumentException e) {
            mostrarErro(e.getMessage());
            if (e.getMessage().contains("Nome")) aplicarBordaErro(txtNome);
            if (e.getMessage().contains("Unidade")) aplicarBordaErro(txtUnidade);
        }
    }

    private void aplicarBordaErro(TextField campo) {
        campo.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2px; -fx-border-radius: 5px;");
    }

    private void limparBordas() {
        txtNome.setStyle(null);
        txtUnidade.setStyle(null);
    }

    private void limparInputs() {
        txtNome.setText("");
        txtUnidade.setText("");
        Platform.runLater(() -> pane.requestFocus());
    }

    private void mostrarErro(String mensagem) {
        exibirNotificacao(erroLabel, "Erro: " + mensagem);
    }

    private void mostrarSucesso(String mensagem) {
        exibirNotificacao(sucessoLabel, mensagem + " com sucesso!");
    }

    private void exibirNotificacao(Label label, String texto) {
        erroLabel.setVisible(false); erroLabel.setManaged(false);
        sucessoLabel.setVisible(false); sucessoLabel.setManaged(false);

        label.setText(texto);
        label.setVisible(true); label.setManaged(true);
        label.setOpacity(0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), label);
        fadeIn.setFromValue(0); fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), label);
        slideIn.setFromY(-10); slideIn.setToY(0);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), label);
            fadeOut.setFromValue(1); fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                label.setVisible(false); label.setManaged(false);
            });
            fadeOut.play();
        });

        fadeIn.play(); slideIn.play(); delay.play();
    }

    private void forcarPrimeiraLetraMaiuscula(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;

            String primeira = newValue.substring(0, 1).toUpperCase();
            String restante = newValue.length() > 1 ? newValue.substring(1) : "";
            String textoFormatado = primeira + restante;

            if (!newValue.equals(textoFormatado)) {
                int posicaoCursor = field.getCaretPosition();
                field.setText(textoFormatado);
                field.positionCaret(posicaoCursor);
            }
        });
    }
}