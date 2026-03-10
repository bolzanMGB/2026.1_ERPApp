package com.gerenciador.controller;
import com.gerenciador.app.DadosRepositorio;
import com.gerenciador.model.fornecedor.Fornecedor;
import com.gerenciador.model.fornecedor.FornecedorPF;
import com.gerenciador.model.fornecedor.FornecedorPJ;
import com.gerenciador.model.pessoa.Endereco;
import com.gerenciador.model.pessoa.Pessoa;
import com.gerenciador.model.pessoa.PessoaFisica;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.animation.FadeTransition;
import javafx.animation.TranslateTransition;
import javafx.stage.Modality;
import javafx.util.Callback;
import javafx.util.Duration;
import java.net.URL;
import java.util.Objects;
import java.util.Optional;
import java.util.ResourceBundle;

public class FornecedoresController implements Initializable {
    @FXML private AnchorPane pane;
    @FXML private VBox pj_fields;
    @FXML private VBox pf_fields;
    @FXML private Label tipoFormularioLabel;
    @FXML private Label h1Formulario;
    @FXML private HBox boxCadastrar;
    @FXML private HBox boxAtualizar;
    @FXML private TextField txtNomePF, txtCpf;
    @FXML private TextField txtNomeResponsavel, txtCnpj, txtNomeFantasia, txtRazaoSocial, txtInscricaoEstadual;
    @FXML private TextField txtTelefone;
    @FXML private TextField txtCidade, txtBairro, txtRua, txtNumeroCasa;
    @FXML private ComboBox<String> filtroTabela;
    @FXML private TextField txtPesquisa;
    @FXML private Label erroLabel;
    @FXML private Label sucessoLabel;
    @FXML private TableView<Fornecedor> tabelaFornecedores;
    @FXML private TableColumn<Fornecedor, String> colId, colNome, colTipo, colDocumento, colTelefone, colCidade;
    @FXML private TableColumn<Fornecedor, Void> colAcoes;
    private FilteredList<Fornecedor> listaFiltrada;
    @FXML private Label labelFiltro;
    private Fornecedor fornecedorEmEdicao = null;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTabela();
        configurarFiltro();
        configurarCss();
        configurarLimitadores();
        configurarAtalhosTeclado();

        javafx.application.Platform.runLater(() -> pane.requestFocus());
    }

    private void configurarAtalhosTeclado() {

        pane.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    cadastrar();
                    break;
                case ESCAPE:
                    if (fornecedorEmEdicao != null) {
                        ativarModoCadastro();
                    }
                    break;
                default:
                    break;
            }
        });
    }
    private void configurarLimitadores() {

        limitarCaracteres(txtNomePF, 60);
        limitarCaracteres(txtNomeFantasia, 60);
        limitarCaracteres(txtNomeResponsavel, 60);
        limitarCaracteres(txtRazaoSocial, 60);
        limitarCaracteres(txtCidade, 40);
        limitarCaracteres(txtBairro, 40);
        limitarCaracteres(txtRua, 40);
        limitarCaracteres(txtNumeroCasa, 10);
    }

    private void limitarCaracteres(TextField field, int limite) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue != null && newValue.length() > limite) {
                field.setText(oldValue);
            }
        });
    }

    public void configurarFiltro() {
        ObservableList<String> opcoes = FXCollections.observableArrayList("Todos", "Pessoa Física", "Pessoa Jurídica");
        filtroTabela.setItems(opcoes);
        filtroTabela.setValue("Todos");
        aplicarMascaraCPF(txtCpf);
        aplicarMascaraCNPJ(txtCnpj);
        aplicarMascaraInscricao(txtInscricaoEstadual);
        aplicarMascaraTelefone(txtTelefone);
        filtroTabela.setOnAction(event -> atualizarFiltros());

        filtroTabela.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                labelFiltro.getStyleClass().add("label-verde");
            } else {
                labelFiltro.getStyleClass().remove("label-verde");
            }
        });

        txtPesquisa.textProperty().addListener((observable, oldValue, newValue) -> {
            atualizarFiltros();
        });
    }

    public void atualizarFiltros() {
        String termoBusca = txtPesquisa.getText().toLowerCase().trim();
        String selecaoTipo = filtroTabela.getValue();

        listaFiltrada.setPredicate(fornecedor -> {
            boolean matchesTipo = true;
            if (selecaoTipo.equals("Pessoa Física")) {
                matchesTipo = (fornecedor instanceof FornecedorPF);
            } else if (selecaoTipo.equals("Pessoa Jurídica")) {
                matchesTipo = (fornecedor instanceof FornecedorPJ);
            }

            if (!matchesTipo) return false;
            if (termoBusca.isEmpty()) return true;

            Pessoa pessoa = (Pessoa) fornecedor;
            return  fornecedor.getNomePrincipal().toLowerCase().contains(termoBusca) ||
                    fornecedor.getDocumento().toLowerCase().contains(termoBusca) ||
                    pessoa.getTelefone().toLowerCase().contains(termoBusca) ||
                    pessoa.getEndereco().getCidade().toLowerCase().contains(termoBusca);
        });
    }

    public void configurarTabela(){
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTipo.setCellValueFactory(cellData -> {
            Fornecedor fornecedor = cellData.getValue();
            if (fornecedor instanceof PessoaFisica) return new SimpleStringProperty("Física");
            return new SimpleStringProperty("Jurídica");
        });
        colDocumento.setCellValueFactory(new PropertyValueFactory<>("documento"));
        colTelefone.setCellValueFactory(new PropertyValueFactory<>("telefone"));
        colCidade.setCellValueFactory(cellData -> {
            Fornecedor f = cellData.getValue();
            if (f != null && f.getEndereco() != null) {
                return new SimpleStringProperty(f.getEndereco().getCidade());
            }
            return new SimpleStringProperty("");
        });
        colAcoes.setCellFactory(criarColunaAcoes());
        listaFiltrada = new FilteredList<>(DadosRepositorio.getFornecedores(), p -> true);

        SortedList<Fornecedor> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tabelaFornecedores.comparatorProperty());
        tabelaFornecedores.setItems(sortedData);
        
        tabelaFornecedores.setColumnResizePolicy(TableView.UNCONSTRAINED_RESIZE_POLICY);
        colId.setMinWidth(50);
        colTipo.setMinWidth(80);
        colNome.setMinWidth(200);
        colDocumento.setMinWidth(150);
        colTelefone.setMinWidth(120);
        colCidade.setMinWidth(120);
        colAcoes.setMinWidth(90);
    }

    private Callback <TableColumn <Fornecedor, Void>, TableCell <Fornecedor, Void>> criarColunaAcoes (){
        return new Callback <>() {
            @Override
            public TableCell <Fornecedor, Void> call(TableColumn<Fornecedor, Void> fornecedorVoidTableColumn) {
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

                        btnDelete.setOnAction(e -> removerFornecedor(getTableView().getItems().get(getIndex())));
                        btnEdit.setOnAction(e -> editarFornecedor(getTableView().getItems().get(getIndex())));
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

    public void removerFornecedor(Fornecedor fornecedor) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação de Exclusão");
        alert.setHeaderText("Remover Fornecedor");
        alert.setContentText("Deseja mesmo remover " + fornecedor.getNomePrincipal() + "?");

        if (pane.getScene() != null) alert.initModality(Modality.APPLICATION_MODAL);

        ButtonType btnSim = new ButtonType("Sim, Remover", ButtonBar.ButtonData.OK_DONE);
        ButtonType btnCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(btnCancelar, btnSim);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == btnSim) {
            DadosRepositorio.removerFornecedor(fornecedor);
            tabelaFornecedores.getItems().remove(fornecedor);
            mostrarSucesso("Fornecedor removido");
        }
    }
    public void editarFornecedor(Fornecedor fornecedor){
        ativarModoEdicao(fornecedor);
        boolean isPF = fornecedor instanceof FornecedorPF;

        if (isPF) {
            if (pj_fields.isVisible()) trocarFormulario();
        } else {
            if (pf_fields.isVisible()) trocarFormulario();
        }

        tipoFormularioLabel.setText("Fornecedor " + fornecedor.getId());

        Pessoa pessoa = (Pessoa) fornecedor;
        txtTelefone.setText(pessoa.getTelefone());
        txtCidade.setText(pessoa.getEndereco().getCidade());
        txtBairro.setText(pessoa.getEndereco().getBairro());
        txtRua.setText(pessoa.getEndereco().getRua());
        txtNumeroCasa.setText(pessoa.getEndereco().getnumeroCasa());

        if (isPF){
            txtNomePF.setText(fornecedor.getNomePrincipal());
            txtCpf.setText(fornecedor.getDocumento());
        } else{
            FornecedorPJ pj = (FornecedorPJ) fornecedor;
            txtNomeFantasia.setText(pj.getNomePrincipal());
            txtCnpj.setText(pj.getDocumento());
            txtNomeResponsavel.setText(pj.getNomeResponsavel());
            txtInscricaoEstadual.setText(pj.getInscricaoEstadual());
            txtRazaoSocial.setText(pj.getRazaoSocial());
        }

        if (pf_fields.isVisible()) txtNomePF.requestFocus();
        else txtNomeFantasia.requestFocus();
    }

    public void configurarCss(){
        URL cssFC = getClass().getResource("/com/gerenciador/css/formularios.css");
        URL cssTC = getClass().getResource("/com/gerenciador/css/tabelas.css");
        if (cssFC != null) pane.getStylesheets().add(cssFC.toExternalForm());
        if (cssTC != null) pane.getStylesheets().add(cssTC.toExternalForm());
    }

    @FXML
    public void trocarFormulario (){
        boolean pfVisivel = pf_fields.isVisible();
        pf_fields.setVisible(!pfVisivel);
        pf_fields.setManaged(!pfVisivel);
        pj_fields.setVisible(pfVisivel);
        pj_fields.setManaged(pfVisivel);

        tipoFormularioLabel.setText(pfVisivel ? "Pessoa Jurídica" : "Pessoa Física");
        limparBordas();
        limparInputs();

        if (pj_fields.isVisible()) {
            txtNomeFantasia.requestFocus();
        } else {
            txtNomePF.requestFocus();
        }
    }

    private void ativarModoEdicao(Fornecedor fornecedor) {
        h1Formulario.setText("Editar Fornecedor");
        fornecedorEmEdicao = fornecedor;
        boxCadastrar.setVisible(false); boxCadastrar.setManaged(false);
        boxAtualizar.setVisible(true); boxAtualizar.setManaged(true);
    }

    public void ativarModoCadastro() {
        h1Formulario.setText("Cadastrar Fornecedor");
        fornecedorEmEdicao = null;
        boxCadastrar.setVisible(true); boxCadastrar.setManaged(true);
        boxAtualizar.setVisible(false); boxAtualizar.setManaged(false);
        limparBordas();
        limparInputs();
        if (pf_fields.isVisible()) {
            tipoFormularioLabel.setText("Pessoa Física");
        } else {
            tipoFormularioLabel.setText("Pessoa Jurídica");
        }

        javafx.application.Platform.runLater(() -> pane.requestFocus());
    }


    @FXML
    public void cadastrar() {
        try {
            limparBordas();
            Endereco endereco = new Endereco(txtCidade.getText(), txtBairro.getText(), txtRua.getText(), txtNumeroCasa.getText());

            if (fornecedorEmEdicao == null) {
                boolean isPF = tipoFormularioLabel.getText().equals("Pessoa Física");

                if (isPF) {
                    DadosRepositorio.adicionarFornecedor(new FornecedorPF(txtNomePF.getText(), txtCpf.getText(), txtTelefone.getText(), endereco));
                } else {
                    DadosRepositorio.adicionarFornecedor(new FornecedorPJ(txtNomeFantasia.getText(), txtTelefone.getText(), endereco, txtCnpj.getText(),
                            txtNomeResponsavel.getText(), txtInscricaoEstadual.getText(), txtRazaoSocial.getText()));
                }
                limparInputs();
                mostrarSucesso("Cadastro realizado");

            } else {
                if (fornecedorEmEdicao instanceof FornecedorPF) {
                    FornecedorPF pf = (FornecedorPF) fornecedorEmEdicao;
                    pf.setTelefone(txtTelefone.getText());
                    pf.setEndereco(endereco);
                    pf.setNome(txtNomePF.getText());
                    pf.setCpf(txtCpf.getText());
                    mostrarSucesso("Atualização realizada");

                } else if (fornecedorEmEdicao instanceof FornecedorPJ) {
                    FornecedorPJ pj = (FornecedorPJ) fornecedorEmEdicao;
                    pj.setTelefone(txtTelefone.getText());
                    pj.setEndereco(endereco);
                    pj.setNome(txtNomeFantasia.getText());
                    pj.setCnpj(txtCnpj.getText());
                    pj.setNomeResponsavel(txtNomeResponsavel.getText());
                    pj.setRazaoSocial(txtRazaoSocial.getText());
                    pj.setInscricaoEstadual(txtInscricaoEstadual.getText());
                    mostrarSucesso("Atualização realizada");
                }
                DadosRepositorio.atualizarFornecedor(fornecedorEmEdicao);
                tabelaFornecedores.refresh();
                ativarModoCadastro();
            }
        } catch (IllegalArgumentException e) {
            lidarComErroValidacao(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Erro inesperado: " + e.getMessage());
        }
    }

    private void lidarComErroValidacao(String msg) {
        mostrarErro(msg);
        if (msg.contains("Fantasia")) { aplicarBordaErro(txtNomePF); aplicarBordaErro(txtNomeFantasia); }
        else if (msg.contains("CPF")) aplicarBordaErro(txtCpf);
        else if (msg.contains("CNPJ")) aplicarBordaErro(txtCnpj);
        else if (msg.contains("Responsável")) aplicarBordaErro(txtNomeResponsavel);
        else if (msg.contains("Inscrição Estadual")) aplicarBordaErro(txtInscricaoEstadual);
        else if (msg.contains("Razão Social")) aplicarBordaErro(txtRazaoSocial);
        else if (msg.contains("Telefone")) aplicarBordaErro(txtTelefone);
        else if (msg.contains("Cidade")) aplicarBordaErro(txtCidade);
        else if (msg.contains("Bairro")) aplicarBordaErro(txtBairro);
        else if (msg.contains("Rua")) aplicarBordaErro(txtRua);
        else if (msg.contains("Número")) aplicarBordaErro(txtNumeroCasa);
    }

    private void mostrarErro(String mensagem) {
        exibirNotificacao(erroLabel, "Erro: " + mensagem);
    }

    private void mostrarSucesso(String mensagem) {
        exibirNotificacao(sucessoLabel, mensagem + " com sucesso!");
    }

    private void exibirNotificacao(Label label, String texto) {
        erroLabel.setVisible(false);
        erroLabel.setManaged(false);
        sucessoLabel.setVisible(false);
        sucessoLabel.setManaged(false);

        label.setText(texto);
        label.setVisible(true);
        label.setManaged(true);
        label.setOpacity(1.0);

        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), label);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        TranslateTransition slideIn = new TranslateTransition(Duration.millis(300), label);
        slideIn.setFromY(-10);
        slideIn.setToY(0);

        PauseTransition delay = new PauseTransition(Duration.seconds(3));
        delay.setOnFinished(e -> {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), label);
            fadeOut.setFromValue(1);
            fadeOut.setToValue(0);
            fadeOut.setOnFinished(event -> {
                label.setVisible(false);
                label.setManaged(false);
            });
            fadeOut.play();
        });

        fadeIn.play();
        slideIn.play();
        delay.play();
    }

    private void aplicarBordaErro(TextField campo) {
        campo.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2px; -fx-border-radius: 5px;");
    }

    private void limparBordas() {
        TextField[] fields = {txtNomePF, txtCpf, txtTelefone, txtCidade, txtBairro, txtRua,
                txtNumeroCasa, txtNomeResponsavel, txtNomeFantasia,
                txtInscricaoEstadual, txtRazaoSocial, txtCnpj};
        for (TextField f : fields) f.setStyle(null);
    }

    private void limparInputs() {
        TextField[] fields = {txtNomePF, txtCpf, txtTelefone, txtCidade, txtBairro, txtRua,
                txtNumeroCasa, txtNomeResponsavel, txtNomeFantasia,
                txtInscricaoEstadual, txtRazaoSocial, txtCnpj};
        for (TextField f : fields) f.setText("");
        Platform.runLater(() -> pane.requestFocus());
    }

    private void aplicarMascaraCPF(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;

            String texto = newValue.replaceAll("[^0-9]", "");
            if (texto.length() > 11) texto = texto.substring(0, 11);

            StringBuilder b = new StringBuilder(texto);
            if (b.length() > 3) b.insert(3, ".");
            if (b.length() > 7) b.insert(7, ".");
            if (b.length() > 11) b.insert(11, "-");

            String resultado = b.toString();
            if (!newValue.equals(resultado)) {
                field.setText(resultado);
                javafx.application.Platform.runLater(field::end);
            }
        });
    }

    private void aplicarMascaraCNPJ(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;

            String texto = newValue.replaceAll("[^0-9]", "");
            if (texto.length() > 14) texto = texto.substring(0, 14);

            StringBuilder b = new StringBuilder(texto);
            if (b.length() > 2) b.insert(2, ".");
            if (b.length() > 6) b.insert(6, ".");
            if (b.length() > 10) b.insert(10, "/");
            if (b.length() > 15) b.insert(15, "-");

            String resultado = b.toString();
            if (!newValue.equals(resultado)) {
                field.setText(resultado);
                javafx.application.Platform.runLater(field::end);
            }
        });
    }

    private void aplicarMascaraInscricao(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;

            String texto = newValue.replaceAll("[^0-9]", "");
            if (texto.length() > 14) texto = texto.substring(0, 14);

            if (!newValue.equals(texto)) {
                field.setText(texto);
                javafx.application.Platform.runLater(field::end);
            }
        });
    }

    private void aplicarMascaraTelefone(TextField field) {
        field.textProperty().addListener((obs, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) return;

            String apenasNumeros = newValue.replaceAll("[^0-9]", "");
            if (apenasNumeros.length() > 11) apenasNumeros = apenasNumeros.substring(0, 11);

            StringBuilder b = new StringBuilder();
            int qtd = apenasNumeros.length();
            if (qtd > 0) b.append("(");
            if (qtd <= 2) b.append(apenasNumeros);
            else {
                b.append(apenasNumeros, 0, 2).append(")");
                if (qtd <= 6) b.append(apenasNumeros.substring(2));
                else if (qtd <= 10) b.append(apenasNumeros, 2, 6).append("-").append(apenasNumeros.substring(6));
                else b.append(apenasNumeros, 2, 7).append("-").append(apenasNumeros.substring(7));
            }

            String resultado = b.toString();
            if (!newValue.equals(resultado)) {
                field.setText(resultado);
                javafx.application.Platform.runLater(field::end);
            }
        });
    }
}