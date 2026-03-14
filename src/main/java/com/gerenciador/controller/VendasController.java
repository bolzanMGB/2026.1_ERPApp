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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.gerenciador.app.DadosRepositorio;
import com.gerenciador.model.cliente.Cliente;
import com.gerenciador.model.comercializavel.Comercializavel;
import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.comercializavel.Produto;
import com.gerenciador.model.transacao.ComposicaoVenda;
import com.gerenciador.model.transacao.Venda;

import javafx.animation.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class VendasController implements Initializable {

    @FXML private AnchorPane pane;
    @FXML private Label h1Formulario, tipoFormularioLabel;
    @FXML private HBox boxCadastrar, boxAtualizar;
    @FXML private ComboBox<String> filtroTipo, filtroStatus;
    @FXML private TextField txtPesquisa, txtNumeroVenda, txtObservacao;
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private ComboBox<BigDecimal> cbNumeroItens;
    @FXML private DatePicker dpDataVenda, dpDataPagamento, dpDataLimite;
    @FXML private Label lblValorTotal;
    @FXML private VBox boxItensContainer, boxPagamento;
    @FXML private CheckBox checkEstaPago;

    @FXML private Button btnUploadPNGvenda;
    @FXML private Label labelArquivoPNG, labelArquivoPdf, erroLabel, sucessoLabel;

    @FXML private TableView<Venda> tabelaVendas;
    @FXML private TableColumn<Venda, Integer> colId, colNumero;
    @FXML private TableColumn<Venda, String> colDataVenda, colCliente, colTipo, colItem, colValor, colStatus, colQtd;
    @FXML private TableColumn<Venda, Void> colAcoes;

    private boolean isCarregandoEdicao = false;
    private FilteredList<Venda> listaFiltrada;
    private Venda vendaEmEdicao = null;
    private File arquivoNotaSelecionado = null;
    private File arquivoComprovanteSelecionado = null;

    private static final String APP_DIR = System.getProperty("user.home") + File.separator + "GerenciadorApp";
    private static final String PDF_VENDAS_DIR = APP_DIR + File.separator + "PDF" + File.separator + "Vendas";

    private class VendaRowUI {
        ComboBox<String> cbTipoVenda;
        ComboBox<Comercializavel> cbItem;
        TextField txtQtd;
        TextField txtValor;
        Label lblTotalItem;
        VBox boxProdutoMP;
        VBox vboxMateriasPrimasLista;
        Map<MateriaPrima, TextField> mapComposicaoInputs = new HashMap<>();

        public VendaRowUI(ComboBox<String> cbT, ComboBox<Comercializavel> cbI, TextField tQ, TextField tV, Label lT, VBox boxMP, VBox vList) {
            this.cbTipoVenda = cbT; this.cbItem = cbI; this.txtQtd = tQ; this.txtValor = tV; this.lblTotalItem = lT; this.boxProdutoMP = boxMP; this.vboxMateriasPrimasLista = vList;
        }
    }
    private List<VendaRowUI> linhasVenda = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        criarPastaDoSistema();
        configurarTabela();
        configurarDatePickers();
        configurarComboBoxes();
        configurarFiltros();
        configurarLimitadores();
        configurarCss();

        // Força apenas números no campo de numero da venda
        txtNumeroVenda.textProperty().addListener((obs, oldText, newText) -> {
            if (!newText.matches("\\d*")) {
                txtNumeroVenda.setText(newText.replaceAll("[^\\d]", ""));
            }
        });

        gerarCamposItens(1);
    }

    private void gerarCamposItens(int quantidade) {
        boxItensContainer.getChildren().clear();
        linhasVenda.clear();

        for (int i = 0; i < quantidade; i++) {
            VBox boxPrincipal = new VBox(5);
            Label lblTitulo = new Label((i + 1) + "° Item");
            lblTitulo.getStyleClass().add("labels-formulario");

            VBox boxItemRow = new VBox(10);
            boxItemRow.setStyle("-fx-border-color: #D3D3D3; -fx-border-radius: 8px; -fx-padding: 15px; -fx-background-color: #FAFAFA; -fx-background-radius: 8px;");

            VBox boxTipo = new VBox(5);
            Label lblTipo = new Label("Tipo de Venda"); lblTipo.getStyleClass().add("labels-formulario");
            ComboBox<String> cbTipo = new ComboBox<>(FXCollections.observableArrayList("Produto", "Matéria-Prima", "Serviço"));
            cbTipo.setMaxWidth(Double.MAX_VALUE); cbTipo.getStyleClass().add("combo-filtro"); cbTipo.setPromptText("Selecionar");
            boxTipo.getChildren().addAll(lblTipo, cbTipo);

            VBox boxNomeItem = new VBox(5);
            Label lblItem = new Label("Item Selecionado"); lblItem.getStyleClass().add("labels-formulario");
            ComboBox<Comercializavel> cbItemRow = new ComboBox<>();
            cbItemRow.setMaxWidth(Double.MAX_VALUE); cbItemRow.getStyleClass().add("combo-filtro"); cbItemRow.setPromptText("Selecionar");
            boxNomeItem.getChildren().addAll(lblItem, cbItemRow);

            HBox hboxQtdValor = new HBox(10);
            VBox boxQtd = new VBox(5); HBox.setHgrow(boxQtd, Priority.ALWAYS);
            Label lblQtd = new Label("Quantidade"); lblQtd.getStyleClass().add("labels-formulario");
            TextField txtQtd = new TextField(); txtQtd.setPromptText("Qtd"); txtQtd.getStyleClass().add("textFields-formulario");
            boxQtd.getChildren().addAll(lblQtd, txtQtd);

            VBox boxValor = new VBox(5); HBox.setHgrow(boxValor, Priority.ALWAYS);
            Label lblValor = new Label("Valor Unidade ($)"); lblValor.getStyleClass().add("labels-formulario");
            TextField txtValor = new TextField(); txtValor.setPromptText("R$ 0,00"); txtValor.getStyleClass().add("textFields-formulario");
            boxValor.getChildren().addAll(lblValor, txtValor);
            hboxQtdValor.getChildren().addAll(boxQtd, boxValor);

            VBox boxTotalRow = new VBox(5);
            boxTotalRow.setMaxWidth(Double.MAX_VALUE);
            Label lblTotalTexto = new Label("Subtotal:"); lblTotalTexto.getStyleClass().add("labels-formulario");
            lblTotalTexto.setMaxWidth(Double.MAX_VALUE);
            Label lblValorTotalItem = new Label("R$ 0,00"); lblValorTotalItem.getStyleClass().add("valorTotal");
            boxTotalRow.getChildren().addAll(lblTotalTexto, lblValorTotalItem);
            lblValorTotalItem.setMaxWidth(Double.MAX_VALUE);
            lblValorTotalItem.setAlignment(Pos.CENTER_RIGHT);

            VBox boxMP = new VBox(5); boxMP.setVisible(false); boxMP.setManaged(false);
            Label lblMP = new Label("Matérias-Primas utilizadas"); lblMP.getStyleClass().add("labels-formulario");
            VBox vboxMPList = new VBox(5);
            boxMP.getChildren().addAll(lblMP, vboxMPList);

            boxItemRow.getChildren().addAll(boxTipo, boxNomeItem, hboxQtdValor, boxTotalRow, boxMP);
            boxPrincipal.getChildren().addAll(lblTitulo, boxItemRow);
            boxItensContainer.getChildren().add(boxPrincipal);

            VendaRowUI row = new VendaRowUI(cbTipo, cbItemRow, txtQtd, txtValor, lblValorTotalItem, boxMP, vboxMPList);
            linhasVenda.add(row);

            configurarMecanicasRowUI(row, lblItem);
        }
    }

    private void configurarMecanicasRowUI(VendaRowUI row, Label lblItem) {
        ObservableList<Comercializavel> listaBaseItens = FXCollections.observableArrayList();

        row.cbTipoVenda.valueProperty().addListener((obs, o, n) -> {
            if (isCarregandoEdicao) return;
            String tipoInterno = "Matéria-Prima".equals(n) ? "MateriaPrima" : n;
            row.cbItem.setValue(null); row.cbItem.getEditor().clear();
            row.txtQtd.clear(); row.txtValor.clear();
            row.boxProdutoMP.setVisible(false); row.boxProdutoMP.setManaged(false); row.vboxMateriasPrimasLista.getChildren().clear(); row.mapComposicaoInputs.clear();
            marcarCampoInvalido(row.cbItem, false);

            if (n != null) {
                lblItem.setText(n + ("Matéria-Prima".equals(n) ? " Vendida" : " Vendido"));
                row.cbItem.setItems(obterItensPorTipo(tipoInterno));
                listaBaseItens.setAll(row.cbItem.getItems());
            } else { lblItem.setText("Item Selecionado"); listaBaseItens.clear(); }
        });

        row.cbItem.setEditable(true);
        row.cbItem.setConverter(new StringConverter<>() {
            @Override public String toString(Comercializavel i) { return i == null ? "" : i.getNome(); }
            @Override public Comercializavel fromString(String s) { return listaBaseItens.stream().filter(i -> i.getNome().equals(s)).findFirst().orElse(null); }
        });

        row.cbItem.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            marcarCampoInvalido(row.cbItem, false);
            if (row.cbItem.getValue() != null && row.cbItem.getConverter().toString(row.cbItem.getValue()).equals(newText)) return;
            if (newText == null || newText.isEmpty()) row.cbItem.setItems(listaBaseItens);
            else {
                String filter = newText.toLowerCase();
                ObservableList<Comercializavel> filtrados = listaBaseItens.filtered(i -> i.getNome().toLowerCase().contains(filter));
                Platform.runLater(() -> {
                    row.cbItem.setItems(filtrados);
                    if (!filtrados.isEmpty()) { row.cbItem.hide(); row.cbItem.setVisibleRowCount(Math.min(filtrados.size(), 10)); row.cbItem.show(); row.cbItem.getEditor().end(); }
                    else row.cbItem.hide();
                });
            }
        });

        row.cbItem.valueProperty().addListener((obs, oldVal, itemSelecionado) -> {
            marcarCampoInvalido(row.cbItem, false);
            row.txtQtd.setPromptText("Quantidade");
            if (itemSelecionado == null) { row.boxProdutoMP.setVisible(false); row.boxProdutoMP.setManaged(false); return; }
            if (itemSelecionado instanceof Produto produto && "Produto".equals(row.cbTipoVenda.getValue())) {
                gerarCamposMatrizComposicao(produto, row);
            } else {
                row.boxProdutoMP.setVisible(false); row.boxProdutoMP.setManaged(false); row.vboxMateriasPrimasLista.getChildren().clear(); row.mapComposicaoInputs.clear();
                if (itemSelecionado instanceof MateriaPrima mp) row.txtQtd.setPromptText("Qtd em " + mp.getUnidade());
            }
        });

        row.txtQtd.textProperty().addListener((obs, o, n) -> { recalcularTotalRow(row); marcarCampoInvalido(row.txtQtd, false); });
        row.txtValor.textProperty().addListener((obs, o, n) -> { recalcularTotalRow(row); marcarCampoInvalido(row.txtValor, false); });
        aplicarMascaraValorUnidade(row.txtQtd); aplicarMascaraValorUnidade(row.txtValor);
    }

    private ObservableList<Comercializavel> obterItensPorTipo(String tipoSelecionado) {
        if ("MateriaPrima".equals(tipoSelecionado)) {
            ObservableList<Comercializavel> listaMP = FXCollections.observableArrayList();
            listaMP.addAll(DadosRepositorio.getMateriasPrimas()); return listaMP;
        } else if ("Produto".equals(tipoSelecionado) || "Serviço".equals(tipoSelecionado)) {
            return DadosRepositorio.getComercializaveis().filtered(c -> c.getTipo().equalsIgnoreCase(tipoSelecionado));
        } return FXCollections.observableArrayList();
    }

    private void gerarCamposMatrizComposicao(Produto produto, VendaRowUI row) {
        row.vboxMateriasPrimasLista.getChildren().clear(); row.mapComposicaoInputs.clear();
        for (MateriaPrima mp : produto.getMateriasPrimasNecessarias()) {
            HBox hbox = new HBox(10); hbox.setAlignment(Pos.CENTER_LEFT);
            Label lbl = new Label(mp.getNome() + " (" + mp.getUnidade() + "):"); lbl.setPrefWidth(200); lbl.getStyleClass().add("labels-formulario");
            TextField txtQtdMP = new TextField(); txtQtdMP.setPromptText("Qtd"); txtQtdMP.getStyleClass().add("textFields-formulario"); txtQtdMP.setPrefWidth(120);
            HBox.setHgrow(txtQtdMP, Priority.ALWAYS); aplicarMascaraValorUnidade(txtQtdMP);

            txtQtdMP.textProperty().addListener((obs, oldV, newV) -> marcarCampoInvalido(txtQtdMP, false));

            row.mapComposicaoInputs.put(mp, txtQtdMP); hbox.getChildren().addAll(lbl, txtQtdMP); row.vboxMateriasPrimasLista.getChildren().add(hbox);
        }
        row.boxProdutoMP.setVisible(true); row.boxProdutoMP.setManaged(true);
    }

    private void recalcularTotalRow(VendaRowUI row) {
        try {
            BigDecimal q = new BigDecimal(row.txtQtd.getText().replace(",", "."));
            BigDecimal v = new BigDecimal(row.txtValor.getText().replace(",", "."));
            row.lblTotalItem.setText("R$ " + q.multiply(v).setScale(2, java.math.RoundingMode.HALF_UP).toString().replace(".", ","));
        } catch (Exception e) { row.lblTotalItem.setText("R$ 0,00"); }
        recalcularTotalGeral();
    }

    private void recalcularTotalGeral() {
        BigDecimal totalGeral = BigDecimal.ZERO;
        for (VendaRowUI r : linhasVenda) {
            try {
                BigDecimal q = new BigDecimal(r.txtQtd.getText().replace(",", ".")); BigDecimal v = new BigDecimal(r.txtValor.getText().replace(",", "."));
                totalGeral = totalGeral.add(q.multiply(v));
            } catch (Exception ignored) {}
        }
        java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00", new java.text.DecimalFormatSymbols(new java.util.Locale("pt", "BR")));
        lblValorTotal.setText("R$ " + df.format(totalGeral));
    }

    private void criarPastaDoSistema() { File diretorio = new File(PDF_VENDAS_DIR); if (!diretorio.exists()) diretorio.mkdirs(); }
    public void configurarCss(){
        URL cssFC = getClass().getResource("/com/gerenciador/css/formularios.css"); URL cssTC = getClass().getResource("/com/gerenciador/css/tabelas.css");
        if (cssFC != null) pane.getStylesheets().add(cssFC.toExternalForm()); if (cssTC != null) pane.getStylesheets().add(cssTC.toExternalForm());
    }

    private void configurarComboBoxes() {
        ObservableList<BigDecimal> ops = FXCollections.observableArrayList(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"), new BigDecimal("4"), new BigDecimal("5"));
        cbNumeroItens.setItems(ops); cbNumeroItens.setValue(new BigDecimal("1"));
        cbNumeroItens.valueProperty().addListener((obs, old, nv) -> { if (nv != null && !isCarregandoEdicao) gerarCamposItens(nv.intValue()); });

        cbCliente.setItems(DadosRepositorio.getCliente()); cbCliente.setEditable(true);
        cbCliente.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) { return c == null ? "" : c.getId() + " | " + c.getNomePrincipal(); }
            @Override public Cliente fromString(String string) { return cbCliente.getItems().stream().filter(c -> toString(c).equals(string)).findFirst().orElse(null); }
        });

        cbCliente.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            marcarCampoInvalido(cbCliente, false);
            if (cbCliente.getValue() != null && cbCliente.getConverter().toString(cbCliente.getValue()).equals(newText)) return;
            if (newText == null || newText.isEmpty()) cbCliente.setItems(DadosRepositorio.getCliente());
            else {
                String filter = newText.toLowerCase(); ObservableList<Cliente> filtrados = DadosRepositorio.getCliente().filtered(c -> c.getId().toString().contains(filter) || c.getNomePrincipal().toLowerCase().contains(filter));
                Platform.runLater(() -> { cbCliente.setItems(filtrados); if (!filtrados.isEmpty()) { cbCliente.hide(); cbCliente.setVisibleRowCount(10); cbCliente.show(); } else cbCliente.hide(); });
            }
        });

        cbCliente.valueProperty().addListener((obs, oldVal, newVal) -> marcarCampoInvalido(cbCliente, false));
        txtNumeroVenda.textProperty().addListener((obs, oldVal, newVal) -> marcarCampoInvalido(txtNumeroVenda, false));
        dpDataVenda.valueProperty().addListener((obs, oldVal, newVal) -> marcarCampoInvalido(dpDataVenda, false));
        dpDataLimite.valueProperty().addListener((obs, oldVal, newVal) -> marcarCampoInvalido(dpDataLimite, false));
        dpDataPagamento.valueProperty().addListener((obs, oldVal, newVal) -> marcarCampoInvalido(dpDataPagamento, false));

        checkEstaPago.selectedProperty().addListener((obs, oldVal, isPago) -> {
            boxPagamento.setVisible(isPago); boxPagamento.setManaged(isPago);
            if (isPago && dpDataPagamento.getValue() == null) dpDataPagamento.setValue(LocalDate.now());
            if (!isPago) { arquivoComprovanteSelecionado = null; labelArquivoPdf.setText("Nenhum arquivo"); labelArquivoPdf.setStyle("-fx-text-fill: #777;"); marcarCampoInvalido(dpDataPagamento, false); }
        });
    }

    private void marcarCampoInvalido(Control campo, boolean invalido) {
        if (invalido) {
            campo.setStyle("-fx-border-color: #E74C3C;-fx-border-width: 2px;  -fx-border-radius: 5px;");
        } else {
            campo.setStyle("");
        }
    }

    private void limparBordasValidacao() {
        marcarCampoInvalido(txtNumeroVenda, false);
        marcarCampoInvalido(dpDataVenda, false);
        marcarCampoInvalido(dpDataLimite, false);
        marcarCampoInvalido(cbCliente, false);
        marcarCampoInvalido(dpDataPagamento, false);

        for (VendaRowUI r : linhasVenda) {
            marcarCampoInvalido(r.cbItem, false);
            marcarCampoInvalido(r.txtQtd, false);
            marcarCampoInvalido(r.txtValor, false);
            for (TextField tfMP : r.mapComposicaoInputs.values()) {
                marcarCampoInvalido(tfMP, false);
            }
        }
    }

    @FXML public void salvar() {
        try {
            limparBordasValidacao();
            boolean isValido = true;

            // Validação dos Campos Globais
            if (txtNumeroVenda.getText() == null || txtNumeroVenda.getText().isBlank()) {
                marcarCampoInvalido(txtNumeroVenda, true); isValido = false;
            }
            if (dpDataVenda.getValue() == null) { marcarCampoInvalido(dpDataVenda, true); isValido = false; }
            if (dpDataLimite.getValue() == null) { marcarCampoInvalido(dpDataLimite, true); isValido = false; }
            if (cbCliente.getValue() == null) { marcarCampoInvalido(cbCliente, true); isValido = false; }

            boolean isPago = checkEstaPago.isSelected();
            if (isPago && dpDataPagamento.getValue() == null) { marcarCampoInvalido(dpDataPagamento, true); isValido = false; }

            // Validação dos Itens Dinâmicos
            for (VendaRowUI r : linhasVenda) {
                Comercializavel item = r.cbItem.getValue();
                String qStr = r.txtQtd.getText() != null ? r.txtQtd.getText().replace(",", ".") : "";
                String vStr = r.txtValor.getText() != null ? r.txtValor.getText().replace(",", ".") : "";

                if (item == null) { marcarCampoInvalido(r.cbItem, true); isValido = false; }
                if (qStr.isBlank()) { marcarCampoInvalido(r.txtQtd, true); isValido = false; }
                if (vStr.isBlank()) { marcarCampoInvalido(r.txtValor, true); isValido = false; }

                if (item instanceof Produto) {
                    for (Map.Entry<MateriaPrima, TextField> entry : r.mapComposicaoInputs.entrySet()) {
                        if (entry.getValue().getText() == null || entry.getValue().getText().isBlank()) {
                            marcarCampoInvalido(entry.getValue(), true); isValido = false;
                        }
                    }
                }
            }

            if (!isValido) {
                mostrarErro("Preencha todos os campos obrigatórios destacados em vermelho.");
                return;
            }

            Integer numVenda = Integer.parseInt(txtNumeroVenda.getText());
            LocalDate dtVenda = dpDataVenda.getValue(); Cliente cliente = cbCliente.getValue(); LocalDate dtLimite = dpDataLimite.getValue();
            LocalDate dtPgto = isPago ? dpDataPagamento.getValue() : null;

            List<Venda.ItemVenda> novosItens = new ArrayList<>();
            for (VendaRowUI r : linhasVenda) {
                Comercializavel item = r.cbItem.getValue();
                BigDecimal qtd = new BigDecimal(r.txtQtd.getText().replace(",", "."));
                BigDecimal val = new BigDecimal(r.txtValor.getText().replace(",", "."));

                ComposicaoVenda comp = null;
                if (item instanceof Produto) {
                    Map<MateriaPrima, BigDecimal> materiasUsadas = new HashMap<>();
                    for (Map.Entry<MateriaPrima, TextField> entry : r.mapComposicaoInputs.entrySet()) {
                        materiasUsadas.put(entry.getKey(), new BigDecimal(entry.getValue().getText().replace(",", ".")));
                    }
                    comp = new ComposicaoVenda(materiasUsadas);
                }
                novosItens.add(new Venda.ItemVenda(item, qtd, val, comp));
            }

            byte[] bNota = null; if (arquivoNotaSelecionado != null) bNota = Files.readAllBytes(arquivoNotaSelecionado.toPath()); else if (vendaEmEdicao != null) bNota = vendaEmEdicao.getNotaFiscal();
            byte[] bComp = null;
            if (isPago) {
                if (arquivoComprovanteSelecionado != null) bComp = Files.readAllBytes(arquivoComprovanteSelecionado.toPath());
                else if (vendaEmEdicao != null) bComp = vendaEmEdicao.getComprovante();
                if (bComp == null && (vendaEmEdicao == null || vendaEmEdicao.getNomeComprovante() == null)) { mostrarErro("Anexe o comprovante."); return; }
            }

            String nmNota = (vendaEmEdicao != null) ? vendaEmEdicao.getNomeNotaFiscal() : null;
            if (arquivoNotaSelecionado != null) { nmNota = "NF_" + System.currentTimeMillis() + "_" + arquivoNotaSelecionado.getName(); Files.copy(arquivoNotaSelecionado.toPath(), new File(PDF_VENDAS_DIR, nmNota).toPath(), StandardCopyOption.REPLACE_EXISTING); }
            String nmComp = (vendaEmEdicao != null) ? vendaEmEdicao.getNomeComprovante() : null;
            if (isPago && arquivoComprovanteSelecionado != null) { nmComp = "COMP_" + System.currentTimeMillis() + "_" + arquivoComprovanteSelecionado.getName(); Files.copy(arquivoComprovanteSelecionado.toPath(), new File(PDF_VENDAS_DIR, nmComp).toPath(), StandardCopyOption.REPLACE_EXISTING); }

            if (vendaEmEdicao == null) {
                Venda nv = new Venda(numVenda, dtVenda, cliente, novosItens, isPago, dtLimite, dtPgto, bNota, bComp, txtObservacao.getText());
                nv.setNomeNotaFiscal(nmNota); nv.setNomeComprovante(nmComp);
                DadosRepositorio.adicionarVenda(nv); mostrarSucesso("Venda cadastrada!");
            } else {
                vendaEmEdicao.atualizarDados(numVenda, dtVenda, cliente, novosItens, isPago, dtLimite, dtPgto, bNota, bComp, txtObservacao.getText());
                vendaEmEdicao.setNomeNotaFiscal(nmNota); vendaEmEdicao.setNomeComprovante(nmComp);
                DadosRepositorio.atualizarVenda(vendaEmEdicao); ativarModoCadastro(); mostrarSucesso("Venda atualizada!");
            }
            limparInputs();
            tabelaVendas.refresh();
            Platform.runLater(() -> pane.requestFocus());
        } catch (Exception e) { mostrarErro(e.getMessage()); }
    }

    private void editarVenda(Venda venda) {
        this.vendaEmEdicao = venda; this.isCarregandoEdicao = true;
        try {
            h1Formulario.setText("Editar Venda"); boxCadastrar.setVisible(false); boxCadastrar.setManaged(false); boxAtualizar.setVisible(true); boxAtualizar.setManaged(true);

            txtNumeroVenda.setText(String.valueOf(venda.getNumeroVenda()));
            cbCliente.setValue(venda.getCliente()); txtObservacao.setText(venda.getObservacao()); dpDataVenda.setValue(venda.getDataTransacao()); dpDataLimite.setValue(venda.getDataLimite());

            cbNumeroItens.setValue(new BigDecimal(venda.getItens().size()));
            gerarCamposItens(venda.getItens().size());

            for (int i = 0; i < venda.getItens().size(); i++) {
                Venda.ItemVenda iv = venda.getItens().get(i); VendaRowUI r = linhasVenda.get(i);
                r.cbTipoVenda.setValue("MateriaPrima".equals(iv.getItem().getTipo()) ? "Matéria-Prima" : iv.getItem().getTipo());
                r.cbItem.setItems(obterItensPorTipo(iv.getItem().getTipo())); r.cbItem.setValue(iv.getItem());
                r.txtQtd.setText(iv.getQuantidade().toString().replace(".", ",")); r.txtValor.setText(iv.getValorUnidade().toString().replace(".", ","));

                if (iv.getItem() instanceof Produto && iv.getComposicao() != null) {
                    gerarCamposMatrizComposicao((Produto)iv.getItem(), r);
                    for (Map.Entry<MateriaPrima, TextField> entry : r.mapComposicaoInputs.entrySet()) {
                        BigDecimal v = iv.getComposicao().ingredientes().get(entry.getKey());
                        if (v != null) entry.getValue().setText(v.toString().replace(".", ","));
                    }
                }
                recalcularTotalRow(r);
            }

            if (venda.getNomeNotaFiscal() != null) { labelArquivoPNG.setText("Ver: " + venda.getNomeNotaFiscal()); labelArquivoPNG.setStyle("-fx-text-fill: #00A593; -fx-cursor: hand; -fx-underline: true;"); }
            checkEstaPago.setSelected(venda.getEstaPago());
            if (venda.getEstaPago()) {
                dpDataPagamento.setValue(venda.getDataPagamento());
                if (venda.getNomeComprovante() != null) { labelArquivoPdf.setText("Ver: " + venda.getNomeComprovante()); labelArquivoPdf.setStyle("-fx-text-fill: #00A593; -fx-cursor: hand; -fx-underline: true;"); }
            }
        } finally { this.isCarregandoEdicao = false; }
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        colNumero.setCellValueFactory(new PropertyValueFactory<>("numeroVenda"));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        colDataVenda.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataTransacao().format(dtf)));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente().getNomePrincipal()));
        colTipo.setCellValueFactory(c -> {
            List<Venda.ItemVenda> itens = c.getValue().getItens();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < itens.size(); i++) {
                String tipo = itens.get(i).getItem().getTipo();
                if ("MateriaPrima".equals(tipo)) tipo = "Matéria-Prima";
                sb.append(tipo);
                if (i < itens.size() - 1) sb.append("\n");
            }
            return new SimpleStringProperty(sb.toString());
        });

        colItem.setCellValueFactory(c -> {
            List<Venda.ItemVenda> itens = c.getValue().getItens();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < itens.size(); i++) {
                sb.append(itens.get(i).getItem().getNome());
                if (i < itens.size() - 1) sb.append("\n");
            }
            return new SimpleStringProperty(sb.toString());
        });

        colQtd.setCellValueFactory(c -> {
            List<Venda.ItemVenda> itens = c.getValue().getItens();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < itens.size(); i++) {
                sb.append(itens.get(i).getQuantidade());
                if (i < itens.size() - 1) sb.append("\n");
            }
            return new SimpleStringProperty(sb.toString());
        });

        colValor.setCellValueFactory(c -> {
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00", new java.text.DecimalFormatSymbols(new java.util.Locale("pt", "BR")));
            return new SimpleStringProperty("R$ " + df.format(c.getValue().getValorTotal()));
        });

        colStatus.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEstaPago() ? "Recebido" : "Pendente"));
        colAcoes.setCellFactory(criarColunaAcoes());

        listaFiltrada = new FilteredList<>(DadosRepositorio.getVendas(), p -> true);
        SortedList<Venda> sortedData = new SortedList<>(listaFiltrada);
        sortedData.comparatorProperty().bind(tabelaVendas.comparatorProperty());
        tabelaVendas.setItems(sortedData);
    }

    private void configurarFiltros() {
        filtroTipo.setItems(FXCollections.observableArrayList("Todos", "Produto", "Matéria-Prima", "Serviço"));
        filtroStatus.setItems(FXCollections.observableArrayList("Todos", "Recebido", "Pendente"));
        filtroTipo.setValue("Todos"); filtroStatus.setValue("Todos");
        filtroTipo.setOnAction(e -> atualizarTabela()); filtroStatus.setOnAction(e -> atualizarTabela());
        txtPesquisa.textProperty().addListener((obs, old, nv) -> atualizarTabela());
    }

    private void atualizarTabela() {
        String busca = txtPesquisa.getText() == null ? "" : txtPesquisa.getText().toLowerCase();
        String tipoUI = filtroTipo.getValue(); String status = filtroStatus.getValue(); String tipoInterno = "Matéria-Prima".equals(tipoUI) ? "MateriaPrima" : tipoUI;

        listaFiltrada.setPredicate(v -> {
            boolean matchBusca = busca.isEmpty() ||
                    String.valueOf(v.getNumeroVenda()).contains(busca) ||
                    v.getCliente().getNomePrincipal().toLowerCase().contains(busca) ||
                    v.getItens().stream().anyMatch(i -> i.getItem().getNome().toLowerCase().contains(busca));
            boolean matchTipo = tipoInterno.equals("Todos") || v.getItens().stream().anyMatch(i -> i.getItem().getTipo().equalsIgnoreCase(tipoInterno));
            boolean matchStatus = status.equals("Todos") || (status.equals("Recebido") ? v.getEstaPago() : !v.getEstaPago());

            return matchBusca && matchTipo && matchStatus;
        });
    }

    @FXML public void ativarModoCadastro() {
        h1Formulario.setText("Cadastrar Venda"); tipoFormularioLabel.setText("Nova Venda"); vendaEmEdicao = null;
        boxCadastrar.setVisible(true); boxCadastrar.setManaged(true); boxAtualizar.setVisible(false); boxAtualizar.setManaged(false); limparInputs();
    }

    private void limparInputs() {
        limparBordasValidacao();
        txtNumeroVenda.clear();
        dpDataVenda.setValue(LocalDate.now()); dpDataLimite.setValue(LocalDate.now().plusMonths(1)); dpDataPagamento.setValue(null);
        cbCliente.setValue(null); txtObservacao.clear(); checkEstaPago.setSelected(false); lblValorTotal.setText("R$ 0,00");
        arquivoNotaSelecionado = null; arquivoComprovanteSelecionado = null;
        labelArquivoPNG.setText("Nenhum arquivo"); labelArquivoPNG.setStyle("-fx-text-fill: #777; -fx-underline: false;");
        labelArquivoPdf.setText("Nenhum arquivo"); labelArquivoPdf.setStyle("-fx-text-fill: #777; -fx-underline: false;");
        cbNumeroItens.setValue(new BigDecimal("1")); gerarCamposItens(1);
    }

    private void removerVenda(Venda venda) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Remover transação?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                if (venda.getNomeNotaFiscal() != null) new File(PDF_VENDAS_DIR, venda.getNomeNotaFiscal()).delete();
                if (venda.getNomeComprovante() != null) new File(PDF_VENDAS_DIR, venda.getNomeComprovante()).delete();
                DadosRepositorio.removerVenda(venda); mostrarSucesso("Venda removida");
            } catch (Exception e) { mostrarErro("Erro ao remover"); }
        }
    }

    private Callback<TableColumn<Venda, Void>, TableCell<Venda, Void>> criarColunaAcoes() {
        return param -> new TableCell<>() {
            private final Button btnEdit = new Button(), btnDelete = new Button();
            private final HBox container = new HBox(10, btnEdit, btnDelete);
            {
                btnEdit.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/com/gerenciador/icons/edit.png"))));
                btnDelete.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/com/gerenciador/icons/delete.png"))));
                ((ImageView)btnEdit.getGraphic()).setFitWidth(15); ((ImageView)btnEdit.getGraphic()).setFitHeight(15);
                ((ImageView)btnDelete.getGraphic()).setFitWidth(15); ((ImageView)btnDelete.getGraphic()).setFitHeight(15);
                container.setAlignment(Pos.CENTER); btnEdit.getStyleClass().add("btn-edit"); btnDelete.getStyleClass().add("btn-delete");
                btnDelete.setOnAction(e -> removerVenda(getTableView().getItems().get(getIndex()))); btnEdit.setOnAction(e -> editarVenda(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : container); }
        };
    }

    @FXML public void selecionarPdf(ActionEvent event) {
        FileChooser fc = new FileChooser(); fc.setTitle("Selecionar Arquivo"); fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos", "*.pdf", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(pane.getScene().getWindow());
        if (f != null) {
            if (btnUploadPNGvenda != null && event.getSource() == btnUploadPNGvenda) { arquivoNotaSelecionado = f; labelArquivoPNG.setText(f.getName()); labelArquivoPNG.setStyle("-fx-text-fill: #00A593; -fx-cursor: hand; -fx-underline: true;"); }
            else { arquivoComprovanteSelecionado = f; labelArquivoPdf.setText(f.getName()); labelArquivoPdf.setStyle("-fx-text-fill: #00A593; -fx-cursor: hand; -fx-underline: true;"); }
        }
    }

    @FXML private void visualizarPdf(MouseEvent event) {
        try {
            boolean isNotaFiscal = (event.getSource() == labelArquivoPNG);
            File arquivoTemp = isNotaFiscal ? arquivoNotaSelecionado : arquivoComprovanteSelecionado;
            String nomeSalvo = null;
            if (arquivoTemp != null) { abrirArquivo(arquivoTemp); return; }
            if (vendaEmEdicao != null) nomeSalvo = isNotaFiscal ? vendaEmEdicao.getNomeNotaFiscal() : vendaEmEdicao.getNomeComprovante();
            if (nomeSalvo == null) { mostrarErro("Nenhum arquivo"); return; }
            File arquivo = new File(PDF_VENDAS_DIR, nomeSalvo);
            if (!arquivo.exists()) { mostrarErro("Arquivo não encontrado."); return; }
            abrirArquivo(arquivo);
        } catch (Exception e) { mostrarErro("Falha ao abrir."); }
    }

    private void abrirArquivo(File file) throws IOException { if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(file); }
    private void aplicarMascaraValorUnidade(TextField tf) { tf.textProperty().addListener((obs, old, nv) -> { if (!nv.matches("\\d*([\\,\\.]\\d*)?")) tf.setText(old); }); }
    private void configurarDatePickers() { dpDataVenda.setValue(LocalDate.now()); dpDataLimite.setValue(LocalDate.now().plusMonths(1)); }
    private void configurarLimitadores() { limitar(txtObservacao, 60); }
    private void limitar(TextField tf, int l) { tf.textProperty().addListener((obs, old, nv) -> { if (nv != null && nv.length() > l) tf.setText(old); }); }
    private void mostrarErro(String m) { exibirNotificacao(erroLabel, "Erro: " + m); }
    private void mostrarSucesso(String m) { exibirNotificacao(sucessoLabel, m); }
    private void exibirNotificacao(Label l, String t) {
        erroLabel.setVisible(false); sucessoLabel.setVisible(false); l.setText(t); l.setVisible(true); l.setManaged(true); l.setOpacity(0);
        FadeTransition fi = new FadeTransition(Duration.millis(300), l); fi.setToValue(1); PauseTransition d = new PauseTransition(Duration.seconds(3)); FadeTransition fo = new FadeTransition(Duration.millis(300), l); fo.setToValue(0); fo.setOnFinished(e -> l.setVisible(false));
        new SequentialTransition(fi, d, fo).play();
    }
}