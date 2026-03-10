package com.gerenciador.controller;

import javafx.application.Platform;
import java.awt.Desktop;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

import com.gerenciador.app.DadosRepositorio;
import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.fornecedor.Fornecedor;
import com.gerenciador.model.transacao.Compra;

import javafx.animation.*;
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
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class ComprasController implements Initializable {

    @FXML private AnchorPane pane;
    @FXML private Label h1Formulario;
    @FXML private Label tipoFormularioLabel;
    @FXML private HBox boxCadastrar;
    @FXML private HBox boxAtualizar;
    @FXML private ComboBox<String> filtroTabela;
    @FXML private ComboBox<Fornecedor> boxFornecedor;
    @FXML private ComboBox<MateriaPrima> boxMateriaPrima;
    @FXML private TextField txtQuantidade, txtValorUnidade, txtObservacao;
    @FXML private DatePicker dataCompra, dataPagamento, dataLimite;
    @FXML private Label txtValorTotal;
    @FXML private CheckBox checkEstaPago;
    @FXML private VBox pagamento_fields;
    @FXML private Button btnUpload;
    @FXML private Label labelArquivoPdf;
    @FXML private TextField txtPesquisa;
    @FXML private Label erroLabel, sucessoLabel, labelFiltro;
    @FXML private StackPane containerMensagens;
    @FXML private TableView<Compra> tabelaCompras;
    @FXML private TableColumn<Compra, Integer> colId;
    @FXML private TableColumn<Compra, String> colDataTransacao, colDataLimite;
    @FXML private TableColumn<Compra, String> colNomeFornecedor, colMateriaPrima, colValorTotal, colEstaPago;
    @FXML private TableColumn<Compra, BigDecimal> colQuantidade;
    @FXML private TableColumn<Compra, Void> colAcoes;

    private FilteredList<Compra> listaFiltrada;
    private Compra compraEmEdicao = null;
    private File arquivoPdfSelecionado = null;

    private static final String APP_DIR = System.getProperty("user.home") + File.separator + "GerenciadorApp";
    private static final String BASE_PDF_DIR = APP_DIR + File.separator + "PDF";
    private static final String PDF_COMPRAS_DIR = BASE_PDF_DIR + File.separator + "Compras";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        criarPastaDoSistema();
        configurarTabela();
        configurarDatePickers();
        configurarComboBoxes();
        configurarFiltro();
        configurarLimitadores();
        configurarAtalhosTeclado();
        configurarCss();

        txtQuantidade.textProperty().addListener((observable, oldValue, newValue) -> configurarValorTotal());
        txtValorUnidade.textProperty().addListener((observable, oldValue, newValue) -> configurarValorTotal());
        txtPesquisa.textProperty().addListener((observable, oldValue, newValue) -> atualizarFiltros());

        labelArquivoPdf.setOnMouseClicked(event -> visualizarPdf());
    }

    private void criarPastaDoSistema() {
        File diretorio = new File(PDF_COMPRAS_DIR);
        if (!diretorio.exists()) diretorio.mkdirs();
    }

    @FXML
    public void selecionarPdf() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Comprovante PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf"));

        File file = fileChooser.showOpenDialog(pane.getScene().getWindow());
        if (file != null) {
            arquivoPdfSelecionado = file;
            labelArquivoPdf.setText(file.getName());
            labelArquivoPdf.setStyle("-fx-text-fill: #00A593; -fx-cursor: hand; -fx-underline: true;");
        }
    }

    @FXML
    private void visualizarPdf() {
        try {
            String nomeArquivo = null;
            if (arquivoPdfSelecionado != null) {
                abrirArquivo(arquivoPdfSelecionado);
                return;
            }
            if (compraEmEdicao != null) {
                nomeArquivo = compraEmEdicao.getNomeArquivoPdf();
            }
            if (nomeArquivo == null) {
                mostrarErro("Nenhum PDF disponível.");
                return;
            }

            File pdf = new File(PDF_COMPRAS_DIR, nomeArquivo);
            if (!pdf.exists()) {
                mostrarErro("Arquivo não encontrado.");
                return;
            }

            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdf);
            } else {
                mostrarErro("Sistema não suporta abrir arquivos.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            mostrarErro("Não foi possível abrir o PDF.");
        }
    }

    private void abrirArquivo(File file) throws IOException {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().open(file);
        } else {
            mostrarErro("Visualização não suportada.");
        }
    }

    @FXML
    public void cadastrar() {
        try {
            LocalDate dtCompra = dataCompra.getValue();
            Fornecedor fornecedor = boxFornecedor.getValue();
            MateriaPrima materiaPrima = boxMateriaPrima.getValue();
            String qtdStr = txtQuantidade.getText() != null ? txtQuantidade.getText().replace(",", ".") : "";
            String valorStr = txtValorUnidade.getText() != null ? txtValorUnidade.getText().replace(",", ".") : "";
            LocalDate dtLimite = dataLimite.getValue();
            boolean isPago = checkEstaPago.isSelected();
            LocalDate dtPagamento = isPago ? dataPagamento.getValue() : null;

            if (dtCompra == null || fornecedor == null || materiaPrima == null || qtdStr.isBlank() || valorStr.isBlank() || dtLimite == null) {
                mostrarErro("Preencha os campos obrigatórios.");
                return;
            }
            if (isPago && dtPagamento == null) {
                mostrarErro("Insira a data de pagamento.");
                return;
            }

            BigDecimal qtd = new BigDecimal(qtdStr);
            BigDecimal valorUnitario = new BigDecimal(valorStr);

            byte[] bytesPdf = null;
            if (isPago) {
                if (arquivoPdfSelecionado != null) bytesPdf = Files.readAllBytes(arquivoPdfSelecionado.toPath());
                else if (compraEmEdicao != null) bytesPdf = compraEmEdicao.getNotaFiscal();

                if (bytesPdf == null && (compraEmEdicao == null || compraEmEdicao.getNomeArquivoPdf() == null)) {
                    mostrarErro("Anexe o comprovante PDF.");
                    return;
                }
            }

            if (compraEmEdicao == null) {
                Compra novaCompra = new Compra(dtCompra, fornecedor, materiaPrima, qtd, valorUnitario, isPago, dtLimite, dtPagamento, bytesPdf, txtObservacao.getText());

                if (isPago && arquivoPdfSelecionado != null) {
                    String nomeFinalPdf = "NF_" + System.currentTimeMillis() + ".pdf";
                    Files.copy(arquivoPdfSelecionado.toPath(), new File(PDF_COMPRAS_DIR, nomeFinalPdf).toPath(), StandardCopyOption.REPLACE_EXISTING);
                    novaCompra.setNomeArquivoPdf(nomeFinalPdf);
                }

                DadosRepositorio.adicionarCompra(novaCompra);
                mostrarSucesso("Compra cadastrada");
            } else {
                String nomeArquivo = compraEmEdicao.getNomeArquivoPdf();

                if (isPago && arquivoPdfSelecionado != null) {
                    if (nomeArquivo == null) nomeArquivo = "NF_" + System.currentTimeMillis() + ".pdf";
                    Files.copy(arquivoPdfSelecionado.toPath(), new File(PDF_COMPRAS_DIR, nomeArquivo).toPath(), StandardCopyOption.REPLACE_EXISTING);
                }

                compraEmEdicao.atualizarDados(dtCompra, fornecedor, materiaPrima, qtd, valorUnitario, isPago, dtLimite, dtPagamento, bytesPdf, txtObservacao.getText());
                compraEmEdicao.setNomeArquivoPdf(nomeArquivo);
                DadosRepositorio.atualizarCompra(compraEmEdicao);
                ativarModoCadastro();
                mostrarSucesso("Atualização realizada");
            }

            limparInputs();
            tabelaCompras.refresh();

        } catch (Exception e) {
            mostrarErro("Erro: " + e.getMessage());
        }
    }

    private void editarCompra(Compra compra) {
        h1Formulario.setText("Editar Compra");
        tipoFormularioLabel.setText("Atualizando Compra");
        compraEmEdicao = compra;

        boxCadastrar.setVisible(false); boxCadastrar.setManaged(false);
        boxAtualizar.setVisible(true); boxAtualizar.setManaged(true);

        boxFornecedor.setDisable(false);
        boxMateriaPrima.setDisable(false);

        dataCompra.setValue(compra.getDataTransacao());
        boxFornecedor.setValue(compra.getFornecedor());
        boxMateriaPrima.setValue(compra.getProduto());
        txtQuantidade.setText(compra.getQuantidade().toString().replace(".", ","));
        txtValorUnidade.setText(compra.getValorUnidade().toString().replace(".", ","));
        dataLimite.setValue(compra.getDataLimite());
        txtObservacao.setText(compra.getObservacao());

        if (compra.getEstaPago()) {
            checkEstaPago.setSelected(true);
            dataPagamento.setValue(compra.getDataPagamento());
            if (compra.getNomeArquivoPdf() != null) {
                labelArquivoPdf.setText("Ver: " + compra.getNomeArquivoPdf());
                labelArquivoPdf.setStyle("-fx-text-fill: #00A593; -fx-cursor: hand; -fx-underline: true;");
            }
        } else {
            checkEstaPago.setSelected(false);
        }
    }

    @FXML
    public void ativarModoCadastro() {
        h1Formulario.setText("Cadastrar Compra");
        tipoFormularioLabel.setText("Nova Compra");
        compraEmEdicao = null;
        boxCadastrar.setVisible(true); boxCadastrar.setManaged(true);
        boxAtualizar.setVisible(false); boxAtualizar.setManaged(false);
        boxFornecedor.setDisable(false);
        boxMateriaPrima.setDisable(false);
        limparInputs();
    }

    private void limparInputs() {
        boxFornecedor.getSelectionModel().clearSelection();
        boxMateriaPrima.getSelectionModel().clearSelection();
        txtQuantidade.clear();
        txtValorUnidade.clear();
        txtValorTotal.setText("0,00");
        txtObservacao.clear();
        checkEstaPago.setSelected(false);
        dataCompra.setValue(LocalDate.now());
        dataLimite.setValue(LocalDate.now().plusMonths(1));
        dataPagamento.setValue(null);
        arquivoPdfSelecionado = null;
        labelArquivoPdf.setText("Nenhum arquivo selecionado");
        labelArquivoPdf.setStyle("-fx-text-fill: #777; -fx-cursor: default; -fx-underline: false;");
        Platform.runLater(() -> pane.requestFocus());
    }

    private void configurarComboBoxes() {
        boxFornecedor.setItems(DadosRepositorio.getFornecedores());
        boxFornecedor.setEditable(true);

        boxFornecedor.setConverter(new StringConverter<>() {
            @Override public String toString(Fornecedor f) { return f == null ? "" : f.getId() + " | " + f.getNomePrincipal(); }
            @Override public Fornecedor fromString(String string) {
                return boxFornecedor.getItems().stream().filter(f -> toString(f).equals(string)).findFirst().orElse(null);
            }
        });

        boxFornecedor.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (boxFornecedor.getValue() != null && boxFornecedor.getConverter().toString(boxFornecedor.getValue()).equals(newText)) return;
            if (newText == null || newText.isEmpty()) {
                boxFornecedor.setItems(DadosRepositorio.getFornecedores());
            } else {
                String filter = newText.toLowerCase();
                ObservableList<Fornecedor> filtrados = DadosRepositorio.getFornecedores().filtered(f ->
                        String.valueOf(f.getId()).contains(filter) ||
                                f.getNomePrincipal().toLowerCase().contains(filter)
                );
                Platform.runLater(() -> {
                    boxFornecedor.setItems(filtrados);
                    if (!filtrados.isEmpty()) { boxFornecedor.hide(); boxFornecedor.setVisibleRowCount(Math.min(filtrados.size(), 10)); boxFornecedor.show(); }
                    else boxFornecedor.hide();
                });
            }
        });

        boxFornecedor.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) { txtQuantidade.clear(); txtValorUnidade.clear(); txtValorTotal.setText("0,00"); }
        });

        boxMateriaPrima.setItems(DadosRepositorio.getMateriasPrimas());
        boxMateriaPrima.setEditable(true);

        boxMateriaPrima.setConverter(new StringConverter<>() {
            @Override public String toString(MateriaPrima mp) { return mp == null ? "" : mp.getNome(); }
            @Override public MateriaPrima fromString(String string) {
                return boxMateriaPrima.getItems().stream().filter(mp -> mp.getNome().equals(string)).findFirst().orElse(null);
            }
        });

        boxMateriaPrima.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (boxMateriaPrima.getValue() != null && boxMateriaPrima.getConverter().toString(boxMateriaPrima.getValue()).equals(newText)) return;
            if (newText == null || newText.isEmpty()) {
                boxMateriaPrima.setItems(DadosRepositorio.getMateriasPrimas());
            } else {
                String filter = newText.toLowerCase();
                ObservableList<MateriaPrima> filtrados = DadosRepositorio.getMateriasPrimas().filtered(mp -> mp.getNome().toLowerCase().contains(filter));
                Platform.runLater(() -> {
                    boxMateriaPrima.setItems(filtrados);
                    if (!filtrados.isEmpty()) { boxMateriaPrima.hide(); boxMateriaPrima.setVisibleRowCount(Math.min(filtrados.size(), 10)); boxMateriaPrima.show(); }
                    else boxMateriaPrima.hide();
                });
            }
        });

        boxMateriaPrima.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(MateriaPrima item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText("Selecionar"); setStyle("-fx-text-fill: #888888;"); }
                else { setText(item.getNome()); setStyle("-fx-text-fill: #333333;"); }
            }
        });

        boxMateriaPrima.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                txtQuantidade.clear(); txtValorUnidade.clear(); txtValorTotal.setText("0,00");
                txtQuantidade.setPromptText("Quantidade em " + newVal.getUnidade());
            } else {
                txtQuantidade.setPromptText("Digite a quantidade");
            }
        });

        checkEstaPago.selectedProperty().addListener((obs, oldVal, isPago) -> {
            pagamento_fields.setVisible(isPago);
            pagamento_fields.setManaged(isPago);
            if (isPago) {
                if (compraEmEdicao == null || dataPagamento.getValue() == null) dataPagamento.setValue(LocalDate.now());
            } else {
                dataPagamento.setValue(null);
                arquivoPdfSelecionado = null;
                labelArquivoPdf.setText("Nenhum arquivo selecionado");
                labelArquivoPdf.setStyle("-fx-text-fill: #777; -fx-cursor: default; -fx-underline: false;");
            }
        });
    }

    private void configurarValorTotal() {
        try {
            BigDecimal q = new BigDecimal(txtQuantidade.getText().replace(",", "."));
            BigDecimal v = new BigDecimal(txtValorUnidade.getText().replace(",", "."));
            BigDecimal total = q.multiply(v).setScale(2, java.math.RoundingMode.HALF_UP);
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            df.setDecimalFormatSymbols(new java.text.DecimalFormatSymbols(new java.util.Locale("pt", "BR")));
            txtValorTotal.setText(df.format(total));
        } catch (Exception e) { txtValorTotal.setText("0,00"); }
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colDataTransacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataTransacao().format(dtf)));
        colDataLimite.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataLimite().format(dtf)));
        colNomeFornecedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFornecedor().getNomePrincipal()));
        colMateriaPrima.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduto().getNome()));
        colQuantidade.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colValorTotal.setCellValueFactory(c -> {
            BigDecimal valor = c.getValue().getValorTotal().setScale(2, java.math.RoundingMode.HALF_UP);
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            df.setDecimalFormatSymbols(new java.text.DecimalFormatSymbols(new java.util.Locale("pt", "BR")));
            return new SimpleStringProperty("R$ " + df.format(valor));
        });
        colEstaPago.setCellValueFactory(c -> {
            Boolean pago = c.getValue().getEstaPago();
            return new SimpleStringProperty(pago != null && pago ? "Realizado" : "Pendente");
        });
        colAcoes.setCellFactory(criarColunaAcoes());

        listaFiltrada = new FilteredList<>(DadosRepositorio.getCompras(), p -> true);

        SortedList<Compra> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tabelaCompras.comparatorProperty());
        tabelaCompras.setItems(sortedData);

    }

    private Callback<TableColumn<Compra, Void>, TableCell<Compra, Void>> criarColunaAcoes() {
        return param -> new TableCell<>() {
            private final Button btnEdit = new Button(), btnDelete = new Button();
            private final HBox container = new HBox(10, btnEdit, btnDelete);
            {
                btnEdit.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/com/gerenciador/icons/edit.png"))));
                btnDelete.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/com/gerenciador/icons/delete.png"))));
                ((ImageView)btnEdit.getGraphic()).setFitWidth(15); ((ImageView)btnEdit.getGraphic()).setFitHeight(15);
                ((ImageView)btnDelete.getGraphic()).setFitWidth(15); ((ImageView)btnDelete.getGraphic()).setFitHeight(15);
                container.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("btn-edit"); btnDelete.getStyleClass().add("btn-delete");
                btnDelete.setOnAction(e -> removerCompra(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> editarCompra(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : container);
            }
        };
    }

    private void removerCompra(Compra compra) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Remover transação?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                if (compra.getNomeArquivoPdf() != null) {
                    File pdf = new File(PDF_COMPRAS_DIR, compra.getNomeArquivoPdf());
                    if (pdf.exists()) pdf.delete();
                }
                DadosRepositorio.removerCompra(compra);
                mostrarSucesso("Item removido");
            } catch (Exception e) {
                mostrarErro("Erro ao remover compra");
            }
        }
    }

    private void configurarFiltro() {
        filtroTabela.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) { labelFiltro.getStyleClass().add("label-verde"); }
            else { labelFiltro.getStyleClass().remove("label-verde"); }
        });
        filtroTabela.setItems(FXCollections.observableArrayList("Todos", "Pago", "Não Pago"));
        filtroTabela.setValue("Todos");
        filtroTabela.setOnAction(e -> atualizarFiltros());
        aplicarMascaraValorUnidade(txtValorUnidade);
        aplicarMascaraValorUnidade(txtQuantidade);
        aplicarMascaraData(dataPagamento);
        aplicarMascaraData(dataLimite);
        aplicarMascaraData(dataCompra);
    }

    @FXML
    public void atualizarFiltros() {
        String busca = txtPesquisa.getText() == null ? "" : txtPesquisa.getText().toLowerCase();
        String status = filtroTabela.getValue();
        listaFiltrada.setPredicate(c -> {
            boolean matchStatus = status.equals("Todos") || (status.equals("Pago") ? c.getEstaPago() : !c.getEstaPago());
            boolean matchBusca = busca.isEmpty() || c.getFornecedor().getNomePrincipal().toLowerCase().contains(busca) || c.getProduto().getNome().toLowerCase().contains(busca);
            return matchStatus && matchBusca;
        });
    }

    private void configurarLimitadores() { limitar(txtQuantidade, 10); limitar(txtValorUnidade, 10); limitar(txtObservacao, 60); }
    private void limitar(TextField f, int l) {
        f.textProperty().addListener((obs, old, nv) -> { if (nv != null && nv.length() > l) f.setText(old); });
    }

    private void aplicarMascaraValorUnidade(TextField f) {
        f.textProperty().addListener((obs, old, nv) -> {
            if (nv == null || nv.isEmpty()) return;
            String t = nv.replaceAll("[^0-9,]", "");
            if (!nv.equals(t)) { f.setText(t); f.positionCaret(t.length()); }
        });
    }

    private void aplicarMascaraData(DatePicker dp) {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dp.setConverter(new StringConverter<LocalDate>() {
            @Override public String toString(LocalDate d) { return d != null ? dtf.format(d) : ""; }
            @Override public LocalDate fromString(String s) { try { return LocalDate.parse(s, dtf); } catch (Exception e) { return null; } }
        });
    }

    private void configurarDatePickers() {
        dataCompra.setValue(LocalDate.now());
        dataLimite.setValue(LocalDate.now().plusMonths(1));
    }

    private void configurarCss() {
        URL f = getClass().getResource("/com/gerenciador/css/formularios.css");
        URL t = getClass().getResource("/com/gerenciador/css/tabelas.css");
        if (f != null) pane.getStylesheets().add(f.toExternalForm());
        if (t != null) pane.getStylesheets().add(t.toExternalForm());
    }

    private void configurarAtalhosTeclado() {
        pane.setOnKeyPressed(e -> {
            if (e.getCode() == javafx.scene.input.KeyCode.ENTER) cadastrar();
            else if (e.getCode() == javafx.scene.input.KeyCode.ESCAPE && compraEmEdicao != null) ativarModoCadastro();
        });
    }

    private void mostrarErro(String m) { exibirNotificacao(erroLabel, "Erro: " + m); }
    private void mostrarSucesso(String m) { exibirNotificacao(sucessoLabel, m); }
    private void exibirNotificacao(Label l, String t) {
        erroLabel.setVisible(false); sucessoLabel.setVisible(false);
        l.setText(t); l.setVisible(true); l.setManaged(true); l.setOpacity(0);
        FadeTransition fi = new FadeTransition(Duration.millis(300), l); fi.setToValue(1);
        PauseTransition d = new PauseTransition(Duration.seconds(3));
        FadeTransition fo = new FadeTransition(Duration.millis(300), l); fo.setToValue(0);
        fo.setOnFinished(e -> { l.setVisible(false); l.setManaged(false); });
        new SequentialTransition(fi, d, fo).play();
    }
}