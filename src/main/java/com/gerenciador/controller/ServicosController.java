package com.gerenciador.controller;

import com.gerenciador.app.DadosRepositorio;
import com.gerenciador.model.comercializavel.Comercializavel;
import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.comercializavel.Produto;
import com.gerenciador.model.comercializavel.Servico;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
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
import javafx.stage.Modality;
import javafx.util.Callback;
import javafx.util.Duration;

import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

public class ServicosController implements Initializable {
    @FXML private AnchorPane pane;
    @FXML private VBox vboxServico, vboxProduto;
    @FXML private HBox boxCadastrarServico, boxAtualizarServico, boxCadastrarProduto, boxAtualizarProduto;
    @FXML private Label h1Formulario;
    @FXML private TextField txtNomeServico, txtNomeProduto;
    @FXML private Label erroLabelServico, sucessoLabelServico, erroLabelProduto, sucessoLabelProduto;
    @FXML private MenuButton menuMateriasPrimas;
    @FXML private ComboBox<String> filtroTabela;
    @FXML private TextField txtPesquisa;
    @FXML private TableView<Comercializavel> tabelaServicos;
    @FXML private TableColumn<Comercializavel, Integer> colId;
    @FXML private TableColumn<Comercializavel, String> colTipo, colNome;
    @FXML private TableColumn<Comercializavel, Double> colTotalVendido;
    @FXML private TableColumn<Comercializavel, Void> colAcoes;
    @FXML private Label labelFiltro;
    private FilteredList<Comercializavel> listaFiltrada;
    private Comercializavel itemEmEdicao = null;
    @FXML private Label labelProduto, labelServico;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarTabela();
        configurarFiltro();
        configurarMenuMateriasPrimas();
        configurarListenersFoco();
        configurarCss();
    }

    private void configurarMenuMateriasPrimas() {

        menuMateriasPrimas.getItems().clear();
        for (MateriaPrima mp : DadosRepositorio.getMateriasPrimas()) {
            CheckMenuItem item = new CheckMenuItem(mp.getNome());
            item.setUserData(mp);
            item.setOnAction(e -> atualizarTextoMenuButton());

            menuMateriasPrimas.getItems().add(item);
        }
    }

    private void atualizarTextoMenuButton() {
        List<String> selecionados = menuMateriasPrimas.getItems().stream()
                .map(mi -> (CheckMenuItem) mi)
                .filter(CheckMenuItem::isSelected)
                .map(CheckMenuItem::getText)
                .collect(Collectors.toList());

        if (selecionados.isEmpty()) {
            menuMateriasPrimas.setText("Selecione");
            menuMateriasPrimas.getStyleClass().remove("menu-selecionado");
        } else {
            menuMateriasPrimas.setText(String.join(", ", selecionados));
            if (!menuMateriasPrimas.getStyleClass().contains("menu-selecionado")) {
                menuMateriasPrimas.getStyleClass().add("menu-selecionado");
            }
        }
    }
    private void configurarListenersFoco() {
        txtNomeServico.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) limparBordasErro(vboxProduto);
        });

        txtNomeProduto.focusedProperty().addListener((obs, old, isFocused) -> {
            if (isFocused) limparBordasErro(vboxServico);
        });

        menuMateriasPrimas.showingProperty().addListener((obs, old, isShowing) -> {
            if (isShowing) {
                limparBordasErro(vboxServico);
                menuMateriasPrimas.setStyle(null);
            }
        });
    }

    private void limparBordasErro(Pane container) {
        container.lookupAll(".textFields-formulario").forEach(node -> node.setStyle(null));
        if (container == vboxServico) {
            erroLabelServico.setVisible(false); erroLabelServico.setManaged(false);
        } else {
            erroLabelProduto.setVisible(false); erroLabelProduto.setManaged(false);
        }
    }

    private void limparTodasBordas() {
        limparBordasErro(vboxServico);
        limparBordasErro(vboxProduto);
    }

    @FXML
    public void editarItem(Comercializavel item) {
        limparTodasBordas();
        itemEmEdicao = item;
        h1Formulario.setText("Editar Serviços");

        if (item instanceof Servico s) {
            labelServico.setText(item.getNome());
            vboxProduto.setDisable(true);
            boxCadastrarServico.setVisible(false); boxCadastrarServico.setManaged(false);
            boxAtualizarServico.setVisible(true); boxAtualizarServico.setManaged(true);
            txtNomeServico.setText(s.getNome());
        } else if (item instanceof Produto p) {
            labelProduto.setText(item.getNome());
            vboxServico.setDisable(true);
            boxCadastrarProduto.setVisible(false); boxCadastrarProduto.setManaged(false);
            boxAtualizarProduto.setVisible(true); boxAtualizarProduto.setManaged(true);
            txtNomeProduto.setText(p.getNome());

            for (MenuItem mi : menuMateriasPrimas.getItems()) {
                CheckMenuItem cmi = (CheckMenuItem) mi;
                MateriaPrima mp = (MateriaPrima) cmi.getUserData();
                cmi.setSelected(p.getMateriasPrimasNecessarias().contains(mp));
            }
            atualizarTextoMenuButton();
        }
    }

    @FXML
    public void cadastrarProduto() {
        try {
            txtNomeProduto.setStyle(null);
            menuMateriasPrimas.setStyle(null);

            List<MateriaPrima> selecionadas = menuMateriasPrimas.getItems().stream()
                    .map(mi -> (CheckMenuItem) mi)
                    .filter(CheckMenuItem::isSelected)
                    .map(cmi -> (MateriaPrima) cmi.getUserData())
                    .collect(Collectors.toList());

            if (txtNomeProduto.getText().isBlank()) {
                txtNomeProduto.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2px;");
                throw new IllegalArgumentException("Nome do produto obrigatório");
            }

            if (selecionadas.isEmpty()) {
                menuMateriasPrimas.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2px;");
                throw new IllegalArgumentException("Selecione a matéria-prima");
            }

            if (itemEmEdicao == null) {
                DadosRepositorio.adicionarProduto(new Produto(txtNomeProduto.getText(), selecionadas));
                mostrarSucesso(sucessoLabelProduto, erroLabelProduto, "Produto cadastrado");
            } else {
                Produto p = (Produto) itemEmEdicao;
                p.setNome(txtNomeProduto.getText());
                p.setMateriasPrimasNecessarias(selecionadas);
                DadosRepositorio.atualizarComercializavel(p);
                tabelaServicos.refresh();
                mostrarSucesso(sucessoLabelProduto, erroLabelProduto, "Produto atualizado");
            }
            ativarModoCadastro();
        } catch (IllegalArgumentException e) {
            mostrarErro(erroLabelProduto, sucessoLabelProduto, e.getMessage());
        }
    }

    @FXML
    public void ativarModoCadastro() {
        itemEmEdicao = null;
        h1Formulario.setText("Cadastrar Serviços");

        labelServico.setText("Novo Serviço");

        labelProduto.setText("Novo Produto");
        limparTodasBordas();

        boxCadastrarServico.setVisible(true); boxCadastrarServico.setManaged(true);
        boxAtualizarServico.setVisible(false); boxAtualizarServico.setManaged(false);
        boxCadastrarProduto.setVisible(true); boxCadastrarProduto.setManaged(true);
        boxAtualizarProduto.setVisible(false); boxAtualizarProduto.setManaged(false);

        vboxServico.setDisable(false);
        vboxProduto.setDisable(false);

        txtNomeServico.clear();
        txtNomeProduto.clear();
        menuMateriasPrimas.getItems().forEach(mi -> ((CheckMenuItem) mi).setSelected(false));
        atualizarTextoMenuButton();
        Platform.runLater(() -> pane.requestFocus());
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colTotalVendido.setCellValueFactory(new PropertyValueFactory<>("totalVendido"));
        colAcoes.setCellFactory(criarColunaAcoes());

        listaFiltrada = new FilteredList<>(DadosRepositorio.getComercializaveis(), p -> true);

        SortedList<Comercializavel> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tabelaServicos.comparatorProperty());
        tabelaServicos.setItems(sortedData);
    }

    private void configurarFiltro() {
        ObservableList<String> opcoes = FXCollections.observableArrayList("Todos", "Serviço", "Produto");
        filtroTabela.setItems(opcoes);
        filtroTabela.setValue("Todos");
        filtroTabela.setOnAction(event -> atualizarFiltros());
        txtPesquisa.textProperty().addListener((obs, oldValue, newValue) -> atualizarFiltros());
        filtroTabela.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                labelFiltro.getStyleClass().add("label-verde");
            } else {
                labelFiltro.getStyleClass().remove("label-verde");
            }
        });
    }

    private void atualizarFiltros() {
        String termoBusca = txtPesquisa.getText().toLowerCase().trim();
        String selecaoTipo = filtroTabela.getValue();
        listaFiltrada.setPredicate(item -> {
            boolean matchesTipo = selecaoTipo.equals("Todos") ||
                    (selecaoTipo.equals("Serviço") && item instanceof Servico) ||
                    (selecaoTipo.equals("Produto") && item instanceof Produto);
            if (!matchesTipo) return false;
            return termoBusca.isEmpty() || item.getNome().toLowerCase().contains(termoBusca);
        });
    }

    private Callback<TableColumn<Comercializavel, Void>, TableCell<Comercializavel, Void>> criarColunaAcoes() {
        return param -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox container = new HBox(10, btnEdit, btnDelete);
            {
                ImageView editIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/gerenciador/icons/edit.png")));
                ImageView deleteIcon = new ImageView(new Image(getClass().getResourceAsStream("/com/gerenciador/icons/delete.png")));
                editIcon.setFitWidth(15); editIcon.setFitHeight(15);
                deleteIcon.setFitWidth(15); deleteIcon.setFitHeight(15);
                btnEdit.setGraphic(editIcon); btnDelete.setGraphic(deleteIcon);
                container.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("btn-edit"); btnDelete.getStyleClass().add("btn-delete");
                btnDelete.setOnAction(e -> removerItem(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> editarItem(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        };
    }

    private void removerItem(Comercializavel item) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmação");
        alert.setHeaderText("Remover " + item.getNome() + "?");
        if (pane.getScene() != null) alert.initModality(Modality.APPLICATION_MODAL);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (item instanceof Servico) {
                Servico s =  (Servico) item;
                DadosRepositorio.removerServico(s);
                mostrarSucesso(sucessoLabelServico, erroLabelServico, "Removido");
            }
            else {
                Produto p =  (Produto) item;
                DadosRepositorio.removerProdutos(p);
                mostrarSucesso(sucessoLabelProduto, erroLabelProduto, "Removido");
            }
        }
    }

    @FXML
    public void cadastrarServico() {
        try {
            txtNomeServico.setStyle(null);
            if (txtNomeServico.getText().isBlank()) throw new IllegalArgumentException("Nome inválido");
            if (itemEmEdicao == null) DadosRepositorio.adicionarServico(new Servico(txtNomeServico.getText()));
            else {
                DadosRepositorio.atualizarComercializavel(itemEmEdicao);
                ((Servico) itemEmEdicao).setNome(txtNomeServico.getText()); tabelaServicos.refresh();
            }

            ativarModoCadastro();
            mostrarSucesso(sucessoLabelServico, erroLabelServico, "Sucesso");
        } catch (Exception e) {
            txtNomeServico.setStyle("-fx-border-color: #E74C3C; -fx-border-width: 2px;");
            mostrarErro(erroLabelServico, sucessoLabelServico, e.getMessage());
        }
    }

    private void mostrarErro(Label lE, Label lS, String m) { exibirNotificacao(lE, lS, lE, "Erro: " + m); }
    private void mostrarSucesso(Label lS, Label lE, String m) { exibirNotificacao(lS, lS, lE, m); }

    private void exibirNotificacao(Label lA, Label lS, Label lE, String t) {
        lE.setVisible(false); lE.setManaged(false);
        lS.setVisible(false); lS.setManaged(false);
        lA.setText(t); lA.setVisible(true); lA.setManaged(true); lA.setOpacity(0);
        FadeTransition fi = new FadeTransition(Duration.millis(300), lA); fi.setFromValue(0); fi.setToValue(1);
        TranslateTransition si = new TranslateTransition(Duration.millis(300), lA); si.setFromY(-10); si.setToY(0);
        PauseTransition d = new PauseTransition(Duration.seconds(3));
        d.setOnFinished(e -> {
            FadeTransition fo = new FadeTransition(Duration.millis(300), lA); fo.setFromValue(1); fo.setToValue(0);
            fo.setOnFinished(ev -> { lA.setVisible(false); lA.setManaged(false); });
            fo.play();
        });
        fi.play(); si.play(); d.play();
    }

    private void configurarCss() {
        URL f = getClass().getResource("/com/gerenciador/css/formularios.css");
        URL t = getClass().getResource("/com/gerenciador/css/tabelas.css");
        if (f != null) pane.getStylesheets().add(f.toExternalForm());
        if (t != null) pane.getStylesheets().add(t.toExternalForm());
    }
}