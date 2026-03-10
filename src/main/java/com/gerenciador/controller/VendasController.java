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
import java.util.HashMap;
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
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.Duration;
import javafx.util.StringConverter;

public class VendasController implements Initializable {

    @FXML private AnchorPane pane;
    @FXML private Label h1Formulario;
    @FXML private Label tipoFormularioLabel;
    @FXML private HBox boxCadastrar;
    @FXML private HBox boxAtualizar;
    @FXML private ComboBox<String> filtroTipo;
    @FXML private ComboBox<String> filtroStatus;
    @FXML private TextField txtPesquisa;
    @FXML private ComboBox<Cliente> cbCliente;
    @FXML private ComboBox<String> cbTipoVenda;
    @FXML private ComboBox<Comercializavel> cbItem;
    @FXML private TextField txtQuantidade, txtValorUnidade, txtObservacao;
    @FXML private DatePicker dpDataVenda, dpDataPagamento, dpDataLimite;
    @FXML private Label lblValorTotal, labelItemSelecionado;
    @FXML private VBox boxProdutoMP;
    @FXML private VBox vboxMateriasPrimasLista;
    private Map<MateriaPrima, TextField> mapComposicaoInputs = new HashMap<>();
    @FXML private CheckBox checkEstaPago;
    @FXML private VBox boxPagamento;
    @FXML private Label labelArquivoPdf;
    private boolean isCarregandoEdicao = false;
    @FXML private Label erroLabel, sucessoLabel;
    @FXML private TableView<Venda> tabelaVendas;
    @FXML private TableColumn<Venda, Integer> colId;
    @FXML private TableColumn<Venda, String> colDataVenda, colCliente, colTipo, colItem, colValor, colStatus;
    @FXML private TableColumn<Venda, BigDecimal> colQtd;
    @FXML private TableColumn<Venda, Void> colAcoes;
    private FilteredList<Venda> listaFiltrada;
    private Venda vendaEmEdicao = null;
    private File arquivoPdfSelecionado = null;

    private static final String APP_DIR = System.getProperty("user.home") + File.separator + "GerenciadorApp";
    private static final String BASE_PDF_DIR = APP_DIR + File.separator + "PDF";
    private static final String PDF_VENDAS_DIR = BASE_PDF_DIR + File.separator + "Vendas";

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        criarPastaDoSistema();
        configurarTabela();
        configurarDatePickers();
        configurarComboBoxes();
        configurarFiltros();
        configurarLimitadores();
        configurarCss();
        txtQuantidade.textProperty().addListener((obs, old, nv) -> configurarValorTotal());
        txtValorUnidade.textProperty().addListener((obs, old, nv) -> configurarValorTotal());

        labelArquivoPdf.setOnMouseClicked(event -> visualizarPdf());
    }

    public void configurarCss(){
        URL cssFC = getClass().getResource("/com/gerenciador/css/formularios.css");
        URL cssTC = getClass().getResource("/com/gerenciador/css/tabelas.css");
        if (cssFC != null) pane.getStylesheets().add(cssFC.toExternalForm());
        if (cssTC != null) pane.getStylesheets().add(cssTC.toExternalForm());
    }

    private void criarPastaDoSistema() {
        File diretorio = new File(PDF_VENDAS_DIR);
        if (!diretorio.exists()) diretorio.mkdirs();
    }

    private void configurarComboBoxes() {
        cbCliente.setItems(DadosRepositorio.getCliente());
        cbCliente.setEditable(true);
        cbCliente.setConverter(new StringConverter<>() {
            @Override public String toString(Cliente c) { return c == null ? "" : c.getId() + " | " + c.getNomePrincipal(); }
            @Override public Cliente fromString(String string) {
                return cbCliente.getItems().stream().filter(c -> toString(c).equals(string)).findFirst().orElse(null);
            }
        });

        cbCliente.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (cbCliente.getValue() != null && cbCliente.getConverter().toString(cbCliente.getValue()).equals(newText)) return;
            if (newText == null || newText.isEmpty()) {
                cbCliente.setItems(DadosRepositorio.getCliente());
            } else {
                String filter = newText.toLowerCase();
                ObservableList<Cliente> filtrados = DadosRepositorio.getCliente().filtered(c ->
                        c.getId().toString().contains(filter) || c.getNomePrincipal().toLowerCase().contains(filter)
                );
                Platform.runLater(() -> {
                    cbCliente.setItems(filtrados);
                    if (!filtrados.isEmpty()) { cbCliente.hide(); cbCliente.setVisibleRowCount(Math.min(filtrados.size(), 10)); cbCliente.show(); }
                    else cbCliente.hide();
                });
            }
        });

        cbTipoVenda.setItems(FXCollections.observableArrayList("Produto", "Matéria-Prima", "Serviço"));
        cbTipoVenda.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Selecionar");
                    setStyle("-fx-text-fill: #888888;");
                } else {
                    setText(item);
                    setStyle("-fx-text-fill: #000000;");
                }
            }
        });
        final ObservableList<Comercializavel> listaBaseItens = FXCollections.observableArrayList();

        cbTipoVenda.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (isCarregandoEdicao) return;

            String tipoInterno = "Matéria-Prima".equals(newVal) ? "MateriaPrima" : newVal;

            cbItem.setValue(null);
            cbItem.getEditor().clear();
            txtQuantidade.clear();
            txtValorUnidade.clear();
            esconderBoxMateriasPrimas();

            if (newVal != null) {
                if (newVal.equals("Matéria-Prima")) labelItemSelecionado.setText(newVal + " Vendida");
                else labelItemSelecionado.setText(newVal + " Vendido");
                atualizarComboItens(tipoInterno);
                listaBaseItens.setAll(cbItem.getItems());
            } else {
                labelItemSelecionado.setText("Item Selecionado");
                listaBaseItens.clear();
            }
        });

        cbItem.setEditable(true);
        cbItem.setConverter(new StringConverter<>() {
            @Override public String toString(Comercializavel item) { return item == null ? "" : item.getNome(); }
            @Override public Comercializavel fromString(String s) {
                return listaBaseItens.stream().filter(i -> i.getNome().equals(s)).findFirst().orElse(null);
            }
        });

        cbItem.getEditor().textProperty().addListener((obs, oldText, newText) -> {
            if (cbItem.getValue() != null && cbItem.getConverter().toString(cbItem.getValue()).equals(newText)) return;
            if (newText == null || newText.isEmpty()) {
                cbItem.setItems(listaBaseItens);
            } else {
                String filter = newText.toLowerCase();
                ObservableList<Comercializavel> filtrados = listaBaseItens.filtered(i -> i.getNome().toLowerCase().contains(filter));
                Platform.runLater(() -> {
                    cbItem.setItems(filtrados);
                    if (!filtrados.isEmpty()) { cbItem.hide(); cbItem.setVisibleRowCount(Math.min(filtrados.size(), 10)); cbItem.show(); cbItem.getEditor().end(); }
                    else cbItem.hide();
                });
            }
        });

        cbItem.valueProperty().addListener((obs, oldVal, itemSelecionado) -> {
            txtQuantidade.setPromptText("Digite a quantidade");
            if (itemSelecionado == null) { esconderBoxMateriasPrimas(); return; }
            txtQuantidade.clear();
            txtValorUnidade.clear();

            if (itemSelecionado instanceof Produto produto && "Produto".equals(cbTipoVenda.getValue())) {
                gerarCamposMatrizComposicao(produto);
            } else {
                esconderBoxMateriasPrimas();
                if (itemSelecionado instanceof MateriaPrima mp) txtQuantidade.setPromptText("Qtd em " + mp.getUnidade());
            }
        });

        checkEstaPago.selectedProperty().addListener((obs, oldVal, isPago) -> {
            boxPagamento.setVisible(isPago);
            boxPagamento.setManaged(isPago);
            if (isPago && dpDataPagamento.getValue() == null) dpDataPagamento.setValue(LocalDate.now());
        });
    }

    private void atualizarComboItens(String tipoSelecionado) {
        cbItem.getSelectionModel().clearSelection();
        if ("MateriaPrima".equals(tipoSelecionado)) {
            ObservableList<Comercializavel> listaMP = FXCollections.observableArrayList();
            listaMP.addAll(DadosRepositorio.getMateriasPrimas());
            cbItem.setItems(listaMP);
        } else if ("Produto".equals(tipoSelecionado) || "Serviço".equals(tipoSelecionado)) {
            ObservableList<Comercializavel> filtrados = DadosRepositorio.getComercializaveis().filtered(
                    c -> c.getTipo().equalsIgnoreCase(tipoSelecionado)
            );
            cbItem.setItems(filtrados);
        } else {
            cbItem.setItems(FXCollections.observableArrayList());
        }
    }

    private void gerarCamposMatrizComposicao(Produto produto) {
        vboxMateriasPrimasLista.getChildren().clear();
        mapComposicaoInputs.clear();
        vboxMateriasPrimasLista.getStyleClass().add("vbox-materias-lista");

        for (MateriaPrima mp : produto.getMateriasPrimasNecessarias()) {
            HBox hbox = new HBox(10);
            hbox.setAlignment(Pos.CENTER_LEFT);
            Label lbl = new Label(mp.getNome() + " (" + mp.getUnidade() + "):");
            lbl.setPrefWidth(200);
            lbl.getStyleClass().add("labels-formulario");

            TextField txtQtd = new TextField();
            txtQtd.setPromptText("Qtd");
            txtQtd.getStyleClass().add("textFields-formulario");
            txtQtd.setPrefWidth(120);
            HBox.setHgrow(txtQtd, Priority.ALWAYS);
            txtQtd.setUserData(mp);
            aplicarMascaraValorUnidade(txtQtd);

            mapComposicaoInputs.put(mp, txtQtd);
            hbox.getChildren().addAll(lbl, txtQtd);
            vboxMateriasPrimasLista.getChildren().add(hbox);
        }
        boxProdutoMP.setVisible(true);
        boxProdutoMP.setManaged(true);
        boxProdutoMP.requestLayout();
    }

    private void esconderBoxMateriasPrimas() {
        boxProdutoMP.setVisible(false);
        boxProdutoMP.setManaged(false);
        vboxMateriasPrimasLista.getChildren().clear();
        mapComposicaoInputs.clear();
    }

    @FXML
    public void salvar() {
        try {

            LocalDate dtVenda = dpDataVenda.getValue();
            LocalDate dtLimite = dpDataLimite.getValue();
            Cliente cliente = cbCliente.getValue();
            Comercializavel item = cbItem.getValue();

            String qtdStr = txtQuantidade.getText() != null ? txtQuantidade.getText().replace(",", ".") : "";
            String valStr = txtValorUnidade.getText() != null ? txtValorUnidade.getText().replace(",", ".") : "";

            if (dtVenda == null || cliente == null || item == null || dtLimite == null || qtdStr.isBlank() || valStr.isBlank()) {
                mostrarErro("Preencha todos os campos obrigatórios.");
                return;
            }

            BigDecimal quantidadeVendida = new BigDecimal(qtdStr);
            BigDecimal valorUnitario = new BigDecimal(valStr);
            boolean isPago = checkEstaPago.isSelected();

            byte[] bytesPdf = null;
            if (isPago && arquivoPdfSelecionado != null) {
                bytesPdf = Files.readAllBytes(arquivoPdfSelecionado.toPath());
            }

            ComposicaoVenda composicao = null;

            if (item instanceof Produto produto) {

                Map<MateriaPrima, BigDecimal> materiasUsadas = new HashMap<>();

                for (Map.Entry<MateriaPrima, TextField> entry : mapComposicaoInputs.entrySet()) {

                    MateriaPrima mp = entry.getKey();
                    String mpVal = entry.getValue().getText().replace(",", ".");

                    if (mpVal.isBlank()) {
                        mostrarErro("Informe a quantidade de " + mp.getNome());
                        return;
                    }

                    BigDecimal qtdUsada = new BigDecimal(mpVal);

                    if (mp.getEstoqueAtual().compareTo(qtdUsada) < 0) {
                        mostrarErro(
                                "Estoque insuficiente de " + mp.getNome() +
                                        ". Disponível: " + mp.getEstoqueAtual() +
                                        " " + mp.getUnidade()
                        );
                        return;
                    }

                    materiasUsadas.put(mp, qtdUsada);
                }

                composicao = new ComposicaoVenda(materiasUsadas);
            }

            if (vendaEmEdicao == null) {

                Venda novaVenda;

                if (item instanceof Produto produto) {
                    novaVenda = new Venda(
                            dtVenda,
                            cliente,
                            produto,
                            quantidadeVendida,
                            valorUnitario,
                            composicao,
                            isPago,
                            dtLimite,
                            dpDataPagamento.getValue(),
                            bytesPdf,
                            txtObservacao.getText()
                    );
                } else {
                    novaVenda = new Venda(
                            dtVenda,
                            cliente,
                            item,
                            quantidadeVendida,
                            valorUnitario,
                            isPago,
                            dtLimite,
                            dpDataPagamento.getValue(),
                            bytesPdf,
                            txtObservacao.getText()
                    );
                }

                if (isPago && arquivoPdfSelecionado != null) {

                    String nomeFinalPdf = "NF_Venda_" + System.currentTimeMillis() + ".pdf";

                    Files.copy(
                            arquivoPdfSelecionado.toPath(),
                            new File(PDF_VENDAS_DIR, nomeFinalPdf).toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                    );

                    novaVenda.setNomeArquivoPdf(nomeFinalPdf);
                }

                DadosRepositorio.adicionarVenda(novaVenda);
                mostrarSucesso("Venda cadastrada!");

            } else {

                vendaEmEdicao.atualizarDados(
                        dtVenda,
                        cliente,
                        item,
                        quantidadeVendida,
                        valorUnitario,
                        composicao,
                        isPago,
                        dtLimite,
                        dpDataPagamento.getValue(),
                        bytesPdf,
                        txtObservacao.getText()
                );

                DadosRepositorio.atualizarVenda(vendaEmEdicao);

                mostrarSucesso("Venda atualizada!");
                ativarModoCadastro();
            }

            tabelaVendas.refresh();

        } catch (Exception e) {

            if (e.getMessage() != null) {
                mostrarErro(e.getMessage());
            } else {
                mostrarErro("Erro ao salvar venda.");
            }
        }
    }

    private ComposicaoVenda obterComposicaoDoFormulario() {
        Map<MateriaPrima, BigDecimal> ingredientes = new HashMap<>();
        for (var node : vboxMateriasPrimasLista.getChildren()) {
            if (node instanceof HBox linha) {
                TextField tf = (TextField) linha.getChildren().get(1);
                MateriaPrima mp = (MateriaPrima) tf.getUserData();
                ingredientes.put(mp, new BigDecimal(tf.getText().replace(",", ".")));
            }
        }
        return new ComposicaoVenda(ingredientes);
    }

    private void configurarTabela() {
        colId.setCellValueFactory(new PropertyValueFactory<>("id"));
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        colDataVenda.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDataTransacao().format(dtf)));
        colCliente.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCliente().getNomePrincipal()));

        colTipo.setCellValueFactory(c -> {
            String tipo = c.getValue().getItem().getTipo();
            if ("MateriaPrima".equals(tipo)) tipo = "Matéria-Prima";
            return new SimpleStringProperty(tipo);
        });

        colItem.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getItem().getNome()));
        colQtd.setCellValueFactory(new PropertyValueFactory<>("quantidade"));
        colValor.setCellValueFactory(c -> {
            BigDecimal valor = c.getValue().getValorTotal().setScale(2, java.math.RoundingMode.HALF_UP);
            java.text.DecimalFormat df = new java.text.DecimalFormat("#,##0.00");
            df.setDecimalFormatSymbols(new java.text.DecimalFormatSymbols(new java.util.Locale("pt", "BR")));
            return new SimpleStringProperty("R$ " + df.format(valor));
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
        filtroTipo.setValue("Todos");
        filtroStatus.setValue("Todos");
        filtroTipo.setOnAction(e -> atualizarTabela());
        filtroStatus.setOnAction(e -> atualizarTabela());
        txtPesquisa.textProperty().addListener((obs, old, nv) -> atualizarTabela());
    }

    private void atualizarTabela() {
        String busca = txtPesquisa.getText() == null ? "" : txtPesquisa.getText().toLowerCase();
        String tipoUI = filtroTipo.getValue();
        String status = filtroStatus.getValue();

        String tipoInterno = "Matéria-Prima".equals(tipoUI) ? "MateriaPrima" : tipoUI;

        listaFiltrada.setPredicate(v -> {
            boolean matchBusca = busca.isEmpty() || v.getCliente().getNomePrincipal().toLowerCase().contains(busca) || v.getItem().getNome().toLowerCase().contains(busca);
            boolean matchTipo = tipoInterno.equals("Todos") || v.getItem().getTipo().equalsIgnoreCase(tipoInterno);
            boolean matchStatus = status.equals("Todos") || (status.equals("Recebido") ? v.getEstaPago() : !v.getEstaPago());
            return matchBusca && matchTipo && matchStatus;
        });
    }

    @FXML public void ativarModoCadastro() {
        h1Formulario.setText("Cadastrar Venda");
        tipoFormularioLabel.setText("Nova Venda");
        vendaEmEdicao = null;
        boxCadastrar.setVisible(true); boxCadastrar.setManaged(true);
        boxAtualizar.setVisible(false); boxAtualizar.setManaged(false);
        limparInputs();
    }

    private void limparInputs() {
        dpDataVenda.setValue(LocalDate.now());
        dpDataLimite.setValue(LocalDate.now().plusMonths(1));
        cbCliente.setValue(null); cbTipoVenda.setValue(null); cbItem.setValue(null);
        txtQuantidade.clear(); txtValorUnidade.clear(); lblValorTotal.setText("R$ 0,00");
        txtObservacao.clear(); checkEstaPago.setSelected(false);
        arquivoPdfSelecionado = null; labelArquivoPdf.setText("Nenhum arquivo selecionado");
        esconderBoxMateriasPrimas();
        labelItemSelecionado.setText("Item Selecionado");
    }

    private void configurarValorTotal() {
        try {
            BigDecimal q = new BigDecimal(txtQuantidade.getText().replace(",", "."));
            BigDecimal v = new BigDecimal(txtValorUnidade.getText().replace(",", "."));
            lblValorTotal.setText("R$ " + q.multiply(v).setScale(2, java.math.RoundingMode.HALF_UP).toString().replace(".", ","));
        } catch (Exception e) { lblValorTotal.setText("R$ 0,00"); }
    }

    private void editarVenda(Venda venda) {
        this.vendaEmEdicao = venda;
        this.isCarregandoEdicao = true;
        try {
            h1Formulario.setText("Editar Venda");
            boxCadastrar.setVisible(false); boxCadastrar.setManaged(false);
            boxAtualizar.setVisible(true); boxAtualizar.setManaged(true);

            String tipoInterno = venda.getItem().getTipo();
            String tipoUI = "MateriaPrima".equals(tipoInterno) ? "Matéria-Prima" : tipoInterno;

            cbTipoVenda.setValue(tipoUI);
            atualizarComboItens(tipoInterno);
            cbCliente.setValue(venda.getCliente());
            cbItem.setValue(venda.getItem());

            txtQuantidade.setText(venda.getQuantidade().toString().replace(".", ","));
            txtValorUnidade.setText(venda.getValorUnidade().toString().replace(".", ","));
            txtObservacao.setText(venda.getObservacao());

            if (venda.getItem() instanceof Produto produto && venda.getComposicao() != null) {
                gerarCamposMatrizComposicao(produto);
                Map<MateriaPrima, BigDecimal> ingredientes = venda.getComposicao().ingredientes();
                for (Map.Entry<MateriaPrima, TextField> entry : mapComposicaoInputs.entrySet()) {
                    BigDecimal v = ingredientes.get(entry.getKey());
                    if (v != null) entry.getValue().setText(v.toString().replace(".", ","));
                }
            }
            checkEstaPago.setSelected(venda.getEstaPago());
            if (venda.getEstaPago()) {
                dpDataPagamento.setValue(venda.getDataPagamento());
                labelArquivoPdf.setText(venda.getNomeArquivoPdf() != null ? venda.getNomeArquivoPdf() : "Sem PDF");
            }
            dpDataVenda.setValue(venda.getDataTransacao());
            dpDataLimite.setValue(venda.getDataLimite());
        } finally { this.isCarregandoEdicao = false; }
    }

    private void removerVenda(Venda venda) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Remover transação?", ButtonType.YES, ButtonType.NO);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            try {
                if (venda.getNomeArquivoPdf() != null) {
                    File pdf = new File(PDF_VENDAS_DIR, venda.getNomeArquivoPdf());
                    if (pdf.exists()) pdf.delete();
                }
                DadosRepositorio.removerVenda(venda);
                mostrarSucesso("Venda removida");
            } catch (Exception e) {
                mostrarErro("Erro ao remover venda");
            }
        }
    }

    private Callback<TableColumn<Venda, Void>, TableCell<Venda, Void>> criarColunaAcoes() {
        return param -> new TableCell<>() {
            private final Button btnEdit = new Button();
            private final Button btnDelete = new Button();
            private final HBox container = new HBox(10, btnEdit, btnDelete);
            {
                btnEdit.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/com/gerenciador/icons/edit.png"))));
                btnDelete.setGraphic(new ImageView(new Image(getClass().getResourceAsStream("/com/gerenciador/icons/delete.png"))));
                ((ImageView)btnEdit.getGraphic()).setFitWidth(15); ((ImageView)btnEdit.getGraphic()).setFitHeight(15);
                ((ImageView)btnDelete.getGraphic()).setFitWidth(15); ((ImageView)btnDelete.getGraphic()).setFitHeight(15);
                container.setAlignment(Pos.CENTER);
                btnEdit.getStyleClass().add("btn-edit"); btnDelete.getStyleClass().add("btn-delete");
                btnDelete.setOnAction(e -> removerVenda(getTableView().getItems().get(getIndex())));
                btnEdit.setOnAction(e -> editarVenda(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) { super.updateItem(item, empty); setGraphic(empty ? null : container); }
        };
    }

    @FXML public void selecionarPdf() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Arquivos PDF", "*.pdf"));
        File f = fc.showOpenDialog(pane.getScene().getWindow());
        if (f != null) { arquivoPdfSelecionado = f; labelArquivoPdf.setText(f.getName()); }
    }

    @FXML
    private void visualizarPdf() {
        try {
            String nomeArquivo = null;
            if (arquivoPdfSelecionado != null) {
                abrirArquivo(arquivoPdfSelecionado);
                return;
            }
            if (vendaEmEdicao != null) {
                nomeArquivo = vendaEmEdicao.getNomeArquivoPdf();
            }
            if (nomeArquivo == null) {
                mostrarErro("Nenhum PDF disponível.");
                return;
            }

            File pdf = new File(PDF_VENDAS_DIR, nomeArquivo);
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

    private void aplicarMascaraValorUnidade(TextField tf) {
        tf.textProperty().addListener((obs, old, nv) -> {
            if (!nv.matches("\\d*([\\,\\.]\\d*)?")) tf.setText(old);
        });
    }

    private void configurarDatePickers() { dpDataVenda.setValue(LocalDate.now()); dpDataLimite.setValue(LocalDate.now().plusMonths(1)); }
    private void configurarLimitadores() { limitar(txtQuantidade, 10); limitar(txtValorUnidade, 10); }
    private void limitar(TextField tf, int l) {
        tf.textProperty().addListener((obs, old, nv) -> { if (nv.length() > l) tf.setText(old); });
    }
    private void mostrarErro(String m) { exibirNotificacao(erroLabel, "Erro: " + m); }
    private void mostrarSucesso(String m) { exibirNotificacao(sucessoLabel, m); }
    private void exibirNotificacao(Label l, String t) {
        erroLabel.setVisible(false); sucessoLabel.setVisible(false);
        l.setText(t); l.setVisible(true); l.setManaged(true); l.setOpacity(0);
        FadeTransition fi = new FadeTransition(Duration.millis(300), l); fi.setToValue(1);
        PauseTransition d = new PauseTransition(Duration.seconds(3));
        FadeTransition fo = new FadeTransition(Duration.millis(300), l); fo.setToValue(0);
        fo.setOnFinished(e -> l.setVisible(false));
        new SequentialTransition(fi, d, fo).play();
    }
}