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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import javafx.event.ActionEvent;
import javafx.scene.input.MouseEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class ComprasController implements Initializable {

    @FXML private AnchorPane pane;
    @FXML private Label h1Formulario, tipoFormularioLabel, txtValorTotal, erroLabel, sucessoLabel, labelFiltro;
    @FXML private HBox boxCadastrar, boxAtualizar;
    @FXML private ComboBox<BigDecimal> cbNumeroItens;
    @FXML private ComboBox<String> filtroTabela;
    @FXML private ComboBox<Fornecedor> boxFornecedor;
    @FXML private TextField txtObservacao, txtPesquisa;
    @FXML private DatePicker dataCompra, dataPagamento, dataLimite;
    @FXML private CheckBox checkEstaPago;
    @FXML private VBox pagamento_fields, boxItensContainer;
    @FXML private StackPane containerMensagens;
    @FXML private Button btnUploadPNGpedido;
    @FXML private Label labelArquivoPng, labelArquivoPdf;

    @FXML private TableView<Compra> tabelaCompras;
    @FXML private TableColumn<Compra, Integer> colId;
    @FXML private TableColumn<Compra, String> colDataTransacao, colDataLimite, colNomeFornecedor, colMateriaPrima, colValorTotal, colEstaPago, colQuantidade;
    @FXML private TableColumn<Compra, Void> colAcoes;

    private FilteredList<Compra> listaFiltrada;
    private Compra compraEmEdicao = null;
    private File arquivoNotaSelecionado = null;
    private File arquivoComprovanteSelecionado = null;

    private static final String APP_DIR = System.getProperty("user.home") + File.separator + "GerenciadorApp";
    private static final String PDF_COMPRAS_DIR = APP_DIR + File.separator + "PDF" + File.separator + "Compras";

    // Classe auxiliar para gerenciar as linhas geradas dinamicamente na UI
    private class CompraRowUI {
        ComboBox<MateriaPrima> cbMateriaPrima;
        TextField txtQtd;
        TextField txtValor;
        Label lblTotalItem;

        public CompraRowUI(ComboBox<MateriaPrima> cb, TextField q, TextField v, Label l) {
            this.cbMateriaPrima = cb; this.txtQtd = q; this.txtValor = v; this.lblTotalItem = l;
        }
    }
    private List<CompraRowUI> linhasCompra = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        criarPastaDoSistema();
        configurarTabela();
        configurarDatePickers();
        configurarComboBoxes();
        configurarFiltro();
        configurarLimitadores();
        configurarCss();
        configurarValidacaoVisualBase();

        txtPesquisa.textProperty().addListener((obs, old, nv) -> atualizarFiltros());
        gerarCamposItens(1); // Inicia com 1 item padrão
    }

    private void gerarCamposItens(int quantidade) {
        boxItensContainer.getChildren().clear();
        linhasCompra.clear();

        for (int i = 0; i < quantidade; i++) {
            VBox boxPrincipal = new VBox(5);
            Label lblTitulo = new Label((i + 1) + "° Matéria-Prima");
            lblTitulo.getStyleClass().add("labels-formulario");

            VBox boxItem = new VBox(10);
            boxItem.setStyle("-fx-border-color: #D3D3D3; -fx-border-radius: 8px; -fx-padding: 15px; -fx-background-color: #FAFAFA; -fx-background-radius: 8px;");

            VBox boxNome = new VBox(5);
            Label lblNome = new Label("Nome Matéria-Prima");
            lblNome.getStyleClass().add("labels-formulario");
            ComboBox<MateriaPrima> combo = new ComboBox<>();
            combo.setMaxWidth(Double.MAX_VALUE);
            combo.setPromptText("Selecionar");
            combo.getStyleClass().add("combo-filtro");
            configurarComboMP(combo);
            boxNome.getChildren().addAll(lblNome, combo);

            HBox hboxQtdValor = new HBox(10);
            VBox boxQtd = new VBox(5);
            HBox.setHgrow(boxQtd, Priority.ALWAYS);
            Label lblQtd = new Label("Quantidade");
            lblQtd.getStyleClass().add("labels-formulario");
            TextField txtQtd = new TextField();
            txtQtd.setPromptText("Qtd");
            txtQtd.getStyleClass().add("textFields-formulario");
            boxQtd.getChildren().addAll(lblQtd, txtQtd);

            VBox boxValor = new VBox(5);
            HBox.setHgrow(boxValor, Priority.ALWAYS);
            Label lblValor = new Label("Valor Unidade ($)");
            lblValor.getStyleClass().add("labels-formulario");
            TextField txtValor = new TextField();
            txtValor.setPromptText("R$ 0,00");
            txtValor.getStyleClass().add("textFields-formulario");
            boxValor.getChildren().addAll(lblValor, txtValor);
            hboxQtdValor.getChildren().addAll(boxQtd, boxValor);

            VBox boxTotalRow = new VBox(5);
            boxTotalRow.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(boxTotalRow, Priority.ALWAYS);
            Label lblTotalTexto = new Label("Subtotal:");
            lblTotalTexto.getStyleClass().add("labels-formulario");
            lblTotalTexto.setMaxWidth(Double.MAX_VALUE);
            Label lblValorTotalItem = new Label("R$ 0,00");
            lblValorTotalItem.getStyleClass().add("valorTotal");
            lblValorTotalItem.setMaxWidth(Double.MAX_VALUE);
            lblValorTotalItem.setAlignment(Pos.CENTER_RIGHT);
            boxTotalRow.getChildren().addAll(lblTotalTexto, lblValorTotalItem);

            boxItem.getChildren().addAll(boxNome, hboxQtdValor, boxTotalRow);
            boxPrincipal.getChildren().addAll(lblTitulo, boxItem);

            boxItensContainer.getChildren().add(boxPrincipal);

            CompraRowUI row = new CompraRowUI(combo, txtQtd, txtValor, lblValorTotalItem);
            linhasCompra.add(row);

            // Listeners para cálculos
            txtQtd.textProperty().addListener((obs, o, n) -> recalcularTotaisRowEGeral(row));
            txtValor.textProperty().addListener((obs, o, n) -> recalcularTotaisRowEGeral(row));
            aplicarMascaraValorUnidade(txtQtd);
            aplicarMascaraValorUnidade(txtValor);

            combo.valueProperty().addListener((obs, old, nv) -> {
                if (nv != null) {
                    txtQtd.setPromptText("Qtd em " + nv.getUnidade());
                    combo.getStyleClass().remove("campo-erro");
                }
            });

            // Listeners para remover a borda de erro dinamicamente
            txtQtd.textProperty().addListener((obs, old, nv) -> txtQtd.getStyleClass().remove("campo-erro"));
            txtValor.textProperty().addListener((obs, old, nv) -> txtValor.getStyleClass().remove("campo-erro"));
        }
    }

    private void recalcularTotaisRowEGeral(CompraRowUI row) {
        try {
            BigDecimal q = new BigDecimal(row.txtQtd.getText().replace(",", "."));
            BigDecimal v = new BigDecimal(row.txtValor.getText().replace(",", "."));
            BigDecimal totalRow = q.multiply(v).setScale(2, java.math.RoundingMode.HALF_UP);
            row.lblTotalItem.setText("R$ " + totalRow.toString().replace(".", ","));
        } catch (Exception e) { row.lblTotalItem.setText("R$ 0,00"); }
        recalcularTotalGeral();
    }

    private void recalcularTotalGeral() {
        BigDecimal totalGeral = BigDecimal.ZERO;
        for (CompraRowUI row : linhasCompra) {
            try {
                BigDecimal q = new BigDecimal(row.txtQtd.getText().replace(",", "."));
                BigDecimal v = new BigDecimal(row.txtValor.getText().replace(",", "."));
                totalGeral = totalGeral.add(q.multiply(v));
            } catch (Exception ignored) {}
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00", new java.text.DecimalFormatSymbols(new java.util.Locale("pt", "BR")));
        txtValorTotal.setText("R$ " + df.format(totalGeral));
    }

    private void configurarComboMP(ComboBox<MateriaPrima> boxMateriaPrima) {
        boxMateriaPrima.setItems(DadosRepositorio.getMateriasPrimas());
        boxMateriaPrima.setEditable(true);
        boxMateriaPrima.setConverter(new StringConverter<>() {
            @Override public String toString(MateriaPrima mp) { return mp == null ? "" : mp.getNome(); }
            @Override public MateriaPrima fromString(String string) { return boxMateriaPrima.getItems().stream().filter(mp -> mp.getNome().equals(string)).findFirst().orElse(null); }
        });
        boxMateriaPrima.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (boxMateriaPrima.getValue() != null && boxMateriaPrima.getConverter().toString(boxMateriaPrima.getValue()).equals(newText)) return;
            if (newText == null || newText.isEmpty()) boxMateriaPrima.setItems(DadosRepositorio.getMateriasPrimas());
            else {
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
    }

    // --- VALIDAÇÃO VISUAL ---

    private void configurarValidacaoVisualBase() {
        boxFornecedor.valueProperty().addListener((obs, old, nv) -> boxFornecedor.getStyleClass().remove("campo-erro"));
        dataCompra.valueProperty().addListener((obs, old, nv) -> dataCompra.getStyleClass().remove("campo-erro"));
        dataLimite.valueProperty().addListener((obs, old, nv) -> dataLimite.getStyleClass().remove("campo-erro"));
    }

    private boolean validarCampos() {
        boolean valido = true;
        removerTodasBordasErro();

        if (boxFornecedor.getValue() == null) { marcarCampoComErro(boxFornecedor); valido = false; }
        if (dataCompra.getValue() == null) { marcarCampoComErro(dataCompra); valido = false; }
        if (dataLimite.getValue() == null) { marcarCampoComErro(dataLimite); valido = false; }

        for (CompraRowUI row : linhasCompra) {
            if (row.cbMateriaPrima.getValue() == null) { marcarCampoComErro(row.cbMateriaPrima); valido = false; }
            if (row.txtQtd.getText() == null || row.txtQtd.getText().trim().isEmpty()) { marcarCampoComErro(row.txtQtd); valido = false; }
            if (row.txtValor.getText() == null || row.txtValor.getText().trim().isEmpty()) { marcarCampoComErro(row.txtValor); valido = false; }
        }

        if (!valido) {
            mostrarErro("Preencha todos os campos obrigatórios.");
        }
        return valido;
    }

    private void marcarCampoComErro(Control controle) {
        if (!controle.getStyleClass().contains("campo-erro")) {
            controle.getStyleClass().add("campo-erro");
        }
    }

    private void removerTodasBordasErro() {
        boxFornecedor.getStyleClass().remove("campo-erro");
        dataCompra.getStyleClass().remove("campo-erro");
        dataLimite.getStyleClass().remove("campo-erro");
        for (CompraRowUI row : linhasCompra) {
            row.cbMateriaPrima.getStyleClass().remove("campo-erro");
            row.txtQtd.getStyleClass().remove("campo-erro");
            row.txtValor.getStyleClass().remove("campo-erro");
        }
    }

    // --- FIM DA VALIDAÇÃO VISUAL ---

    private void criarPastaDoSistema() {
        File diretorio = new File(PDF_COMPRAS_DIR);
        if (!diretorio.exists()) diretorio.mkdirs();
    }

    @FXML public void selecionarPdf(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecionar Arquivo");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos", "*.pdf", "*.png", "*.jpg", "*.jpeg"));
        File file = fileChooser.showOpenDialog(pane.getScene().getWindow());
        if (file != null) {
            if (event.getSource() == btnUploadPNGpedido) {
                arquivoNotaSelecionado = file; labelArquivoPng.setText(file.getName()); labelArquivoPng.setStyle("-fx-text-fill: #00A593; -fx-cursor: hand; -fx-underline: true;");
            } else {
                arquivoComprovanteSelecionado = file; labelArquivoPdf.setText(file.getName()); labelArquivoPdf.setStyle("-fx-text-fill: #00A593; -fx-cursor: hand; -fx-underline: true;");
            }
        }
    }

    @FXML private void visualizarPdf(MouseEvent event) {
        try {
            boolean isNotaFiscal = (event.getSource() == labelArquivoPng);
            File arquivoTemp = isNotaFiscal ? arquivoNotaSelecionado : arquivoComprovanteSelecionado;
            String nomeSalvo = null;
            if (arquivoTemp != null) { abrirArquivo(arquivoTemp); return; }
            if (compraEmEdicao != null) nomeSalvo = isNotaFiscal ? compraEmEdicao.getNomeNotaFiscal() : compraEmEdicao.getNomeComprovante();
            if (nomeSalvo == null) { mostrarErro("Nenhum arquivo disponível."); return; }
            File arquivo = new File(PDF_COMPRAS_DIR, nomeSalvo);
            if (!arquivo.exists()) { mostrarErro("Arquivo não encontrado."); return; }
            abrirArquivo(arquivo);
        } catch (Exception e) { mostrarErro("Não foi possível abrir."); }
    }

    private void abrirArquivo(File file) throws IOException {
        if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file);
    }

    @FXML public void cadastrar() {
        if (!validarCampos()) return;

        try {
            LocalDate dtCompra = dataCompra.getValue();
            Fornecedor fornecedor = boxFornecedor.getValue();
            LocalDate dtLimite = dataLimite.getValue();
            boolean isPago = checkEstaPago.isSelected();
            LocalDate dtPagamento = isPago ? dataPagamento.getValue() : null;

            if (isPago && dtPagamento == null) { mostrarErro("Insira a data de pagamento."); return; }

            List<Compra.ItemCompra> novosItens = new ArrayList<>();
            for (CompraRowUI row : linhasCompra) {
                MateriaPrima mp = row.cbMateriaPrima.getValue();
                String qtdStr = row.txtQtd.getText().replace(",", ".");
                String valStr = row.txtValor.getText().replace(",", ".");
                novosItens.add(new Compra.ItemCompra(mp, new BigDecimal(qtdStr), new BigDecimal(valStr)));
            }

            byte[] bytesNota = null;
            if (arquivoNotaSelecionado != null) bytesNota = Files.readAllBytes(arquivoNotaSelecionado.toPath());
            else if (compraEmEdicao != null) bytesNota = compraEmEdicao.getNotaFiscal();

            byte[] bytesComprovante = null;
            if (isPago) {
                if (arquivoComprovanteSelecionado != null) bytesComprovante = Files.readAllBytes(arquivoComprovanteSelecionado.toPath());
                else if (compraEmEdicao != null) bytesComprovante = compraEmEdicao.getComprovante();
                if (bytesComprovante == null && (compraEmEdicao == null || compraEmEdicao.getNomeComprovante() == null)) { mostrarErro("Anexe o comprovante."); return; }
            }

            String nomeNota = (compraEmEdicao != null) ? compraEmEdicao.getNomeNotaFiscal() : null;
            if (arquivoNotaSelecionado != null) {
                nomeNota = "NF_" + System.currentTimeMillis() + "_" + arquivoNotaSelecionado.getName();
                Files.copy(arquivoNotaSelecionado.toPath(), new File(PDF_COMPRAS_DIR, nomeNota).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }
            String nomeComprovante = (compraEmEdicao != null) ? compraEmEdicao.getNomeComprovante() : null;
            if (isPago && arquivoComprovanteSelecionado != null) {
                nomeComprovante = "COMP_" + System.currentTimeMillis() + "_" + arquivoComprovanteSelecionado.getName();
                Files.copy(arquivoComprovanteSelecionado.toPath(), new File(PDF_COMPRAS_DIR, nomeComprovante).toPath(), StandardCopyOption.REPLACE_EXISTING);
            }

            if (compraEmEdicao == null) {
                Compra novaCompra = new Compra(dtCompra, fornecedor, novosItens, isPago, dtLimite, dtPagamento, bytesNota, bytesComprovante, txtObservacao.getText());
                novaCompra.setNomeNotaFiscal(nomeNota); novaCompra.setNomeComprovante(nomeComprovante);
                DadosRepositorio.adicionarCompra(novaCompra);
                mostrarSucesso("Compra cadastrada!");
            } else {
                compraEmEdicao.atualizarDados(dtCompra, fornecedor, novosItens, isPago, dtLimite, dtPagamento, bytesNota, bytesComprovante, txtObservacao.getText());
                compraEmEdicao.setNomeNotaFiscal(nomeNota); compraEmEdicao.setNomeComprovante(nomeComprovante);
                DadosRepositorio.atualizarCompra(compraEmEdicao);
                ativarModoCadastro();
                mostrarSucesso("Atualização realizada!");
            }
            limparInputs();
            tabelaCompras.refresh();

            Platform.runLater(() -> pane.requestFocus());
        } catch (Exception e) { mostrarErro("Erro: " + e.getMessage()); }
    }

    private void editarCompra(Compra compra) {
        h1Formulario.setText("Editar Compra"); tipoFormularioLabel.setText("Atualizando Compra");
        compraEmEdicao = compra;
        boxCadastrar.setVisible(false); boxCadastrar.setManaged(false);
        boxAtualizar.setVisible(true); boxAtualizar.setManaged(true);

        dataCompra.setValue(compra.getDataTransacao());
        boxFornecedor.setValue(compra.getFornecedor());
        dataLimite.setValue(compra.getDataLimite());
        txtObservacao.setText(compra.getObservacao());

        cbNumeroItens.setValue(new BigDecimal(compra.getItens().size())); // Gera automaticamente as UI Rows via listener

        for (int i = 0; i < compra.getItens().size(); i++) {
            Compra.ItemCompra item = compra.getItens().get(i);
            CompraRowUI row = linhasCompra.get(i);
            row.cbMateriaPrima.setValue(item.getMateriaPrima());
            row.txtQtd.setText(item.getQuantidade().toString().replace(".", ","));
            row.txtValor.setText(item.getValorUnidade().toString().replace(".", ","));
        }

        if (compra.getNomeNotaFiscal() != null) { labelArquivoPng.setText("Ver: " + compra.getNomeNotaFiscal()); labelArquivoPng.setStyle("-fx-text-fill: #00A593; -fx-cursor: hand; -fx-underline: true;"); }
        if (compra.getEstaPago()) {
            checkEstaPago.setSelected(true); dataPagamento.setValue(compra.getDataPagamento());
            if (compra.getNomeComprovante() != null) { labelArquivoPdf.setText("Ver: " + compra.getNomeComprovante()); labelArquivoPdf.setStyle("-fx-text-fill: #00A593; -fx-cursor: hand; -fx-underline: true;"); }
        } else checkEstaPago.setSelected(false);

        removerTodasBordasErro();
    }

    @FXML public void ativarModoCadastro() {
        h1Formulario.setText("Cadastrar Compra"); tipoFormularioLabel.setText("Nova Compra");
        compraEmEdicao = null;
        boxCadastrar.setVisible(true); boxCadastrar.setManaged(true);
        boxAtualizar.setVisible(false); boxAtualizar.setManaged(false);
        limparInputs();
    }

    private void limparInputs() {
        boxFornecedor.getSelectionModel().clearSelection(); txtValorTotal.setText("R$ 0,00");
        txtObservacao.clear(); checkEstaPago.setSelected(false);
        dataCompra.setValue(LocalDate.now()); dataLimite.setValue(LocalDate.now().plusMonths(1)); dataPagamento.setValue(null);
        arquivoNotaSelecionado = null; arquivoComprovanteSelecionado = null;
        labelArquivoPng.setText("Nenhum arquivo"); labelArquivoPng.setStyle("-fx-text-fill: #777; -fx-underline: false;");
        labelArquivoPdf.setText("Nenhum arquivo"); labelArquivoPdf.setStyle("-fx-text-fill: #777; -fx-underline: false;");
        cbNumeroItens.setValue(new BigDecimal("1"));
        gerarCamposItens(1);
        removerTodasBordasErro();
    }

    private void configurarComboBoxes() {
        ObservableList<BigDecimal> configNumItens = FXCollections.observableArrayList(
                new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"), new BigDecimal("4"), new BigDecimal("5")
        );
        cbNumeroItens.setItems(configNumItens);
        cbNumeroItens.setValue(new BigDecimal("1"));
        cbNumeroItens.valueProperty().addListener((obs, old, nv) -> { if (nv != null) gerarCamposItens(nv.intValue()); });

        boxFornecedor.setItems(DadosRepositorio.getFornecedores());
        boxFornecedor.setEditable(true);
        boxFornecedor.setConverter(new StringConverter<>() {
            @Override public String toString(Fornecedor f) { return f == null ? "" : f.getId() + " | " + f.getNomePrincipal(); }
            @Override public Fornecedor fromString(String s) { return boxFornecedor.getItems().stream().filter(f -> toString(f).equals(s)).findFirst().orElse(null); }
        });

        checkEstaPago.selectedProperty().addListener((obs, oldVal, isPago) -> {
            pagamento_fields.setVisible(isPago); pagamento_fields.setManaged(isPago);
            if (isPago) { if (compraEmEdicao == null || dataPagamento.getValue() == null) dataPagamento.setValue(LocalDate.now()); }
            else { dataPagamento.setValue(null); arquivoComprovanteSelecionado = null; labelArquivoPdf.setText("Nenhum arquivo"); labelArquivoPdf.setStyle("-fx-text-fill: #777;"); }
        });
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colDataTransacao.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataTransacao().format(dtf)));
        colDataLimite.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataLimite().format(dtf)));
        colNomeFornecedor.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFornecedor().getNomePrincipal()));

        colMateriaPrima.setCellValueFactory(c -> {
            List<Compra.ItemCompra> itens = c.getValue().getItens();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < itens.size(); i++) {
                sb.append(i + 1).append(itens.get(i).getMateriaPrima().getNome());

                if (i < itens.size() - 1) {
                    sb.append("\n");
                }
            }

            return new SimpleStringProperty(sb.toString());
        });

        colQuantidade.setCellValueFactory(c -> {
            List<Compra.ItemCompra> itens = c.getValue().getItens();
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < itens.size(); i++) {
                sb.append(i + 1).append(itens.get(i).getQuantidade());

                if (i < itens.size() - 1) {
                    sb.append("\n");
                }
            }

            return new SimpleStringProperty(sb.toString());
        });
        colValorTotal.setCellValueFactory(c -> {
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00", new java.text.DecimalFormatSymbols(new java.util.Locale("pt", "BR")));
            return new SimpleStringProperty("R$ " + df.format(c.getValue().getValorTotal()));
        });
        colEstaPago.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstaPago() ? "Realizado" : "Pendente"));
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
                container.setAlignment(Pos.CENTER); btnEdit.getStyleClass().add("btn-edit"); btnDelete.getStyleClass().add("btn-delete");
                btnDelete.setOnAction(e -> removerCompra(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> editarCompra(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : container); }
        };
    }

    private void removerCompra(Compra compra) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Remover transação?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                if (compra.getNomeNotaFiscal() != null) new File(PDF_COMPRAS_DIR, compra.getNomeNotaFiscal()).delete();
                if (compra.getNomeComprovante() != null) new File(PDF_COMPRAS_DIR, compra.getNomeComprovante()).delete();
                DadosRepositorio.removerCompra(compra); mostrarSucesso("Item removido");
            } catch (Exception e) { mostrarErro("Erro ao remover"); }
        }
    }

    private void configurarFiltro() {
        filtroTabela.setItems(FXCollections.observableArrayList("Todos", "Pago", "Não Pago"));
        filtroTabela.setValue("Todos"); filtroTabela.setOnAction(e -> atualizarFiltros());
    }

    @FXML public void atualizarFiltros() {
        String busca = txtPesquisa.getText() == null ? "" : txtPesquisa.getText().toLowerCase();
        String status = filtroTabela.getValue();
        listaFiltrada.setPredicate(c -> {
            boolean matchStatus = status.equals("Todos") || (status.equals("Pago") ? c.getEstaPago() : !c.getEstaPago());
            boolean matchBusca = busca.isEmpty() || c.getFornecedor().getNomePrincipal().toLowerCase().contains(busca) ||
                    c.getItens().stream().anyMatch(i -> i.getMateriaPrima().getNome().toLowerCase().contains(busca));
            return matchStatus && matchBusca;
        });
    }

    private void configurarLimitadores() { limitar(txtObservacao, 60); }
    private void limitar(TextField f, int l) { f.textProperty().addListener((obs, old, nv) -> { if (nv != null && nv.length() > l) f.setText(old); }); }
    private void aplicarMascaraValorUnidade(TextField f) { f.textProperty().addListener((obs, old, nv) -> { if (nv == null || nv.isEmpty()) return; String t = nv.replaceAll("[^0-9,]", ""); if (!nv.equals(t)) { f.setText(t); f.positionCaret(t.length()); } }); }
    private void configurarDatePickers() { dataCompra.setValue(LocalDate.now()); dataLimite.setValue(LocalDate.now().plusMonths(1)); }
    private void configurarCss() {
        URL f = getClass().getResource("/com/gerenciador/css/formularios.css"); URL t = getClass().getResource("/com/gerenciador/css/tabelas.css");
        if (f != null) pane.getStylesheets().add(f.toExternalForm()); if (t != null) pane.getStylesheets().add(t.toExternalForm());
    }
    private void mostrarErro(String m) { exibirNotificacao(erroLabel, "Erro: " + m); }
    private void mostrarSucesso(String m) { exibirNotificacao(sucessoLabel, m); }
    private void exibirNotificacao(Label l, String t) {
        erroLabel.setVisible(false); sucessoLabel.setVisible(false); l.setText(t); l.setVisible(true); l.setManaged(true); l.setOpacity(0);
        FadeTransition fi = new FadeTransition(Duration.millis(300), l); fi.setToValue(1); PauseTransition d = new PauseTransition(Duration.seconds(3)); FadeTransition fo = new FadeTransition(Duration.millis(300), l); fo.setToValue(0); fo.setOnFinished(e -> { l.setVisible(false); l.setManaged(false); });
        new SequentialTransition(fi, d, fo).play();
    }
}