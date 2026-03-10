package com.gerenciador.controller;

import com.gerenciador.app.DadosRepositorio;
import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.comercializavel.Produto;
import com.gerenciador.model.comercializavel.Servico;
import com.gerenciador.model.transacao.Compra;
import com.gerenciador.model.transacao.Venda;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.net.URL;
import java.text.NumberFormat;
import java.util.*;

public class RelatoriosController implements Initializable {

    @FXML private AnchorPane pane;
    @FXML private ComboBox<String> filtroPeriodo, filtroMes, filtroAnalise;
    @FXML private ComboBox<Integer> filtroAno;
    @FXML private Label labelFiltroPeriodo, labelFiltroAno, labelFiltroMes, labelModulo;
    @FXML private TextField campoPesquisa;
    @FXML private VBox boxCompras;
    @FXML private VBox boxVendas;
    @FXML private Label labelValorTotalCompras, labelNumeroCompras, labelFornecedoresTotais, labelKilosComprados;
    @FXML private VBox boxGraficoEvolucaoCompras;
    @FXML private BarChart<String, Number> graficoBarrasEvolucaoCompras;
    @FXML private CategoryAxis eixoXMesesCompras;
    @FXML private PieChart graficoPizzaMaterias;
    @FXML private VBox legendaMaterias;
    @FXML private PieChart graficoPizzaFornecedores;
    @FXML private VBox legendaFornecedores;
    @FXML private TableView<ResumoRanking> tabelaFornecedores;
    @FXML private TableColumn<ResumoRanking, String> colFornNome, colForId, colForTipo;
    @FXML private TableColumn<ResumoRanking, Integer> colFornPedidos;
    @FXML private TableColumn<ResumoRanking, String> colFornValor;
    @FXML private TableView<ResumoRanking> tabelaProdutos;
    @FXML private TableColumn<ResumoRanking, String> colProdNome;
    @FXML private TableColumn<ResumoRanking, Integer> colProdPedidos;
    @FXML private TableColumn<ResumoRanking, String> colProdValor;
    @FXML private Label labelValorTotalVendas, labelNumeroVendas, labelClientesTotais, labelKilosVendidos;
    @FXML private VBox boxGraficoEvolucaoVendas;
    @FXML private BarChart<String, Number> graficoBarrasEvolucaoVendas;
    @FXML private CategoryAxis eixoXMesesVendas;
    @FXML private PieChart graficoPizzaTiposVenda;
    @FXML private VBox legendaTiposVenda;
    @FXML private TableView<ResumoRanking> tabelaTiposVenda;
    @FXML private TableColumn<ResumoRanking, String> colTipoVendaNome;
    @FXML private TableColumn<ResumoRanking, Integer> colTipoVendaPedidos;
    @FXML private TableColumn<ResumoRanking, String> colTipoVendaValor;
    @FXML private PieChart graficoPizzaMateriasVenda;
    @FXML private VBox legendaMateriasVenda;
    @FXML private TableView<ResumoRanking> tabelaMateriasVenda;
    @FXML private TableColumn<ResumoRanking, String> colMatVendaNome, colMatVendaId;
    @FXML private TableColumn<ResumoRanking, Integer> colMatVendaPedidos;
    @FXML private TableColumn<ResumoRanking, String> colMatVendaValor;
    @FXML private PieChart graficoPizzaProdutos;
    @FXML private VBox legendaProdutos;
    @FXML private TableView<ResumoRanking> tabelaProdutosVenda;
    @FXML private TableColumn<ResumoRanking, String> colProdVendaNome, colProdVendaId;
    @FXML private TableColumn<ResumoRanking, Integer> colProdVendaPedidos;
    @FXML private TableColumn<ResumoRanking, String> colProdVendaValor;
    @FXML private PieChart graficoPizzaServicos;
    @FXML private VBox legendaServicos;
    @FXML private TableView<ResumoRanking> tabelaServicos;
    @FXML private TableColumn<ResumoRanking, String> colServVendaNome, colServVendaId;
    @FXML private TableColumn<ResumoRanking, Integer> colServVendaPedidos;
    @FXML private TableColumn<ResumoRanking, String> colServVendaValor;
    @FXML private PieChart graficoPizzaClientes;
    @FXML private VBox legendaClientes;
    @FXML private TableView<ResumoRanking> tabelaClientes;
    @FXML private TableColumn<ResumoRanking, String> colCliNome, colCliId, colCliTipo;
    @FXML private TableColumn<ResumoRanking, Integer> colCliPedidos;
    @FXML private TableColumn<ResumoRanking, String> colCliValor;
    private final String[] CORES_GRAFICO = {"#4285F4", "#EA4335", "#FBBC05", "#34A853", "#8AB4F8", "#F28B82"};
    private List<FilteredList<ResumoRanking>> listasFiltradasAtivas = new ArrayList<>();
    
    public static String capitalize(String s) {
        return s == null || s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        configurarCss();
        configurarTabelas();
        configurarFiltro();
        configurarPesquisa();
        atualizarDashboard();
    }

    private void configurarPesquisa() {
        if (campoPesquisa != null) {
            campoPesquisa.textProperty().addListener((observable, oldValue, newValue) -> {
                aplicarFiltroPesquisa(newValue);
            });
        }
    }

    private void aplicarFiltroPesquisa(String termoPesquisa) {
        String termo = (termoPesquisa == null) ? "" : termoPesquisa.toLowerCase().trim();

        for (FilteredList<ResumoRanking> listaFiltrada : listasFiltradasAtivas) {
            listaFiltrada.setPredicate(item -> {

                if (termo.isEmpty()) {
                    return true;
                }

                if (item.getNome() != null && item.getNome().toLowerCase().contains(termo)) return true;

                if (item.getId() != null && item.getId().toLowerCase().contains(termo)) return true;

                if (item.getTipo() != null && item.getTipo().toLowerCase().contains(termo)) return true;

                return false;
            });
        }
    }

    private void configurarFiltro() {
        filtroPeriodo.setItems(FXCollections.observableArrayList("Total", "Anual", "Mensal"));
        filtroAnalise.setItems(FXCollections.observableArrayList("Compras", "Vendas"));

        filtroAno.setItems(DadosRepositorio.getAnos());
        if (!filtroAno.getItems().isEmpty()) {
            filtroAno.setValue(filtroAno.getItems().getLast());
        }

        if(filtroAno.getValue() != null && !DadosRepositorio.getMesesComDados(filtroAno.getValue()).isEmpty()) {
            filtroMes.setValue(DadosRepositorio.getMesesComDados(filtroAno.getValue()).getLast());
        }

        filtroAnalise.valueProperty().addListener((obs, oldVal, newVal) -> {
            atualizarLabelModulo();
            boolean isCompras = "Compras".equals(newVal);
            boxCompras.setVisible(isCompras);
            boxCompras.setManaged(isCompras);
            boxVendas.setVisible(!isCompras);
            boxVendas.setManaged(!isCompras);
            atualizarDashboard();
        });

        filtroPeriodo.valueProperty().addListener((obs, oldV, newV) -> {
            if ("Total".equals(newV)) {
                filtroAno.setDisable(true);
                filtroMes.setDisable(true);
            } else if ("Anual".equals(newV)) {
                filtroAno.setDisable(false);
                filtroMes.setDisable(true);
            } else if ("Mensal".equals(newV)) {
                filtroAno.setDisable(false);
                filtroMes.setDisable(false);
                atualizarComboMeses(filtroAno.getValue());
            }
            atualizarDashboard();
            atualizarLabelModulo();
        });

        filtroAno.valueProperty().addListener((obs, oldAno, novoAno) -> {
            if (novoAno != null && "Mensal".equals(filtroPeriodo.getValue())) {
                atualizarComboMeses(novoAno);
            }
            atualizarDashboard();
        });

        filtroMes.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) atualizarDashboard();
        });

        filtroPeriodo.setValue("Total");
        filtroAnalise.setValue("Compras");
    }

    private void atualizarLabelModulo() {
        String analise = filtroAnalise.getValue();
        String periodo = filtroPeriodo.getValue();
        if (analise == null) {
            return;
        }
        labelModulo.setText((analise.equals("Compras") ? "Módulo de Compras " : "Módulo de Vendas ") + periodo);
    }

    private void atualizarComboMeses(Integer ano) {
        if (ano == null) return;
        ObservableList<String> mesesDisponiveis = DadosRepositorio.getMesesComDados(ano);
        filtroMes.setItems(mesesDisponiveis);
        if (!mesesDisponiveis.isEmpty()) {
            filtroMes.setValue(mesesDisponiveis.getLast());
        }
    }

    private void atualizarDashboard() {
        listasFiltradasAtivas.clear();

        if ("Compras".equals(filtroAnalise.getValue())) {
            atualizarDashboardCompras();
        } else {
            atualizarDashboardVendas();
        }

        if (campoPesquisa != null) {
            aplicarFiltroPesquisa(campoPesquisa.getText());
        }
    }

    private void atualizarDashboardCompras() {
        String periodo = filtroPeriodo.getValue();
        Integer ano = filtroAno.getValue();
        String mes = filtroMes.getValue();

        List<Compra> compras = DadosRepositorio.getComprasFiltradas(periodo, ano, mes);

        BigDecimal totalGasto = compras.stream().map(Compra::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        long numFornecedores = compras.stream().map(c -> c.getFornecedor().getNomePrincipal()).distinct().count();
        BigDecimal kilosComprados = compras.stream().map(Compra::getQuantidade).reduce(BigDecimal.ZERO, BigDecimal::add);

        labelKilosComprados.setText(String.format(new Locale("pt", "BR"), "%.2f kg", kilosComprados.doubleValue()));
        labelValorTotalCompras.setText(NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(totalGasto));
        labelNumeroCompras.setText(String.valueOf(compras.size()));
        labelFornecedoresTotais.setText(String.valueOf(numFornecedores));

        Map<String, ResumoRanking> mapaForn = new HashMap<>();
        Map<String, ResumoRanking> mapaMat = new HashMap<>();

        for (Compra c : compras) {
            String nomeForn = c.getFornecedor().getNomePrincipal();
            if (!mapaForn.containsKey(nomeForn)) {
                ResumoRanking rr = new ResumoRanking(nomeForn);
                try { rr.setId(String.valueOf(c.getFornecedor().getId())); } catch (Exception e) {}
                rr.setTipo(c.getFornecedor().getClass().getSimpleName().replace("Fornecedor", ""));
                mapaForn.put(nomeForn, rr);
            }
            mapaForn.get(nomeForn).adicionar(c.getValorTotal());

            String nomeMat = c.getProduto().getNome();
            mapaMat.putIfAbsent(nomeMat, new ResumoRanking(nomeMat));
            mapaMat.get(nomeMat).adicionar(c.getValorTotal());
        }

        if(mapaForn.isEmpty()) mapaForn.put("Vazio", new ResumoRanking("Nenhum Fornecedor"));
        if(mapaMat.isEmpty()) mapaMat.put("Vazio", new ResumoRanking("Nenhuma Matéria-Prima"));

        preencherTabelaEPizza(mapaForn, tabelaFornecedores, graficoPizzaFornecedores, legendaFornecedores);
        preencherTabelaEPizza(mapaMat, tabelaProdutos, graficoPizzaMaterias, legendaMaterias);

        if ("Anual".equals(periodo) && ano != null) {
            boxGraficoEvolucaoCompras.setVisible(true);
            boxGraficoEvolucaoCompras.setManaged(true);
            processarGraficoEvolucaoGeral(compras, null, graficoBarrasEvolucaoCompras, eixoXMesesCompras);
        } else {
            boxGraficoEvolucaoCompras.setVisible(false);
            boxGraficoEvolucaoCompras.setManaged(false);
        }
    }

    private void atualizarDashboardVendas() {
        String periodo = filtroPeriodo.getValue();
        Integer ano = filtroAno.getValue();
        String mes = filtroMes.getValue();

        List<Venda> vendas = DadosRepositorio.getVendasFiltradas(periodo, ano, mes);

        BigDecimal totalVendido = vendas.stream().map(Venda::getValorTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        long numClientes = vendas.stream().map(v -> v.getCliente().getNomePrincipal()).distinct().count();
        BigDecimal kilosVendidos = vendas.stream().map(Venda::getQuantidade).reduce(BigDecimal.ZERO, BigDecimal::add);

        labelKilosVendidos.setText(String.format(new Locale("pt", "BR"), "%.2f kg", kilosVendidos.doubleValue()));
        labelValorTotalVendas.setText(NumberFormat.getCurrencyInstance(new Locale("pt", "BR")).format(totalVendido));
        labelNumeroVendas.setText(String.valueOf(vendas.size()));
        labelClientesTotais.setText(String.valueOf(numClientes));

        Map<String, ResumoRanking> mapaTipos = new LinkedHashMap<>();
        mapaTipos.put("Matéria-Prima", new ResumoRanking("Matéria-Prima"));
        mapaTipos.put("Produto", new ResumoRanking("Produto"));
        mapaTipos.put("Serviço", new ResumoRanking("Serviço"));

        Map<String, ResumoRanking> mapaMat = new HashMap<>();
        Map<String, ResumoRanking> mapaProd = new HashMap<>();
        Map<String, ResumoRanking> mapaServ = new HashMap<>();
        Map<String, ResumoRanking> mapaCli = new HashMap<>();

        for (Venda v : vendas) {
            String nomeCli = v.getCliente().getNomePrincipal();
            if (!mapaCli.containsKey(nomeCli)) {
                ResumoRanking rr = new ResumoRanking(nomeCli);
                try { rr.setId(String.valueOf(v.getCliente().getId())); } catch (Exception e) {}
                rr.setTipo(v.getCliente().getClass().getSimpleName().replace("Cliente", ""));
                mapaCli.put(nomeCli, rr);
            }
            mapaCli.get(nomeCli).adicionar(v.getValorTotal());

            String tipoItem = "";
            String nomeItem = v.getItem().getNome();

            if (v.getItem() instanceof MateriaPrima) {
                tipoItem = "Matéria-Prima";
                if (!mapaMat.containsKey(nomeItem)) {
                    ResumoRanking rr = new ResumoRanking(nomeItem);
                    try { rr.setId(String.valueOf(v.getItem().getId())); } catch (Exception e) {}
                    mapaMat.put(nomeItem, rr);
                }
                mapaMat.get(nomeItem).adicionar(v.getValorTotal());
            } else if (v.getItem() instanceof Produto) {
                tipoItem = "Produto";
                if (!mapaProd.containsKey(nomeItem)) {
                    ResumoRanking rr = new ResumoRanking(nomeItem);
                    try { rr.setId(String.valueOf(v.getItem().getId())); } catch (Exception e) {}
                    mapaProd.put(nomeItem, rr);
                }
                mapaProd.get(nomeItem).adicionar(v.getValorTotal());
            } else if (v.getItem() instanceof Servico) {
                tipoItem = "Serviço";
                if (!mapaServ.containsKey(nomeItem)) {
                    ResumoRanking rr = new ResumoRanking(nomeItem);
                    try { rr.setId(String.valueOf(v.getItem().getId())); } catch (Exception e) {}
                    mapaServ.put(nomeItem, rr);
                }
                mapaServ.get(nomeItem).adicionar(v.getValorTotal());
            }

            if (!tipoItem.isEmpty()) {
                mapaTipos.get(tipoItem).adicionar(v.getValorTotal());
            }
        }

        if(mapaMat.isEmpty()) mapaMat.put("Vazio", new ResumoRanking("Nenhuma Matéria-Prima"));
        if(mapaProd.isEmpty()) mapaProd.put("Vazio", new ResumoRanking("Nenhum Produto"));
        if(mapaServ.isEmpty()) mapaServ.put("Vazio", new ResumoRanking("Nenhum Serviço"));
        if(mapaCli.isEmpty()) mapaCli.put("Vazio", new ResumoRanking("Nenhum Cliente"));

        preencherTabelaEPizza(mapaTipos, tabelaTiposVenda, graficoPizzaTiposVenda, legendaTiposVenda);
        preencherTabelaEPizza(mapaMat, tabelaMateriasVenda, graficoPizzaMateriasVenda, legendaMateriasVenda);
        preencherTabelaEPizza(mapaProd, tabelaProdutosVenda, graficoPizzaProdutos, legendaProdutos);
        preencherTabelaEPizza(mapaServ, tabelaServicos, graficoPizzaServicos, legendaServicos);
        preencherTabelaEPizza(mapaCli, tabelaClientes, graficoPizzaClientes, legendaClientes);

        if ("Anual".equals(periodo) && ano != null) {
            boxGraficoEvolucaoVendas.setVisible(true);
            boxGraficoEvolucaoVendas.setManaged(true);
            processarGraficoEvolucaoGeral(null, vendas, graficoBarrasEvolucaoVendas, eixoXMesesVendas);
        } else {
            boxGraficoEvolucaoVendas.setVisible(false);
            boxGraficoEvolucaoVendas.setManaged(false);
        }
    }

    private void preencherTabelaEPizza(Map<String, ResumoRanking> mapa, TableView<ResumoRanking> tabela, PieChart pizza, VBox legenda) {
        ObservableList<ResumoRanking> listaOriginais = FXCollections.observableArrayList(mapa.values());
        

        listaOriginais.sort((r1, r2) -> r2.getValorDecimal().compareTo(r1.getValorDecimal()));
        
        FilteredList<ResumoRanking> filteredData = new FilteredList<>(listaOriginais, p -> true);
        listasFiltradasAtivas.add(filteredData);

        SortedList<ResumoRanking> sortedData = new SortedList<>(filteredData);

        sortedData.comparatorProperty().bind(tabela.comparatorProperty());

        tabela.setItems(sortedData);
        
        atualizarGraficoPizza(pizza, legenda, listaOriginais);
    }

    private void atualizarGraficoPizza(PieChart grafico, VBox boxLegenda, ObservableList<ResumoRanking> lista) {
        grafico.getData().clear();
        boxLegenda.getChildren().clear();

        BigDecimal somaTotal = lista.stream().map(ResumoRanking::getValorDecimal).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (somaTotal.compareTo(BigDecimal.ZERO) <= 0) {
            Label semDados = new Label("Sem dados para exibir no gráfico");
            semDados.setStyle("-fx-text-fill: #999999; -fx-font-style: italic; -fx-font-size: 14px;");
            boxLegenda.getChildren().add(semDados);
            return;
        }

        ObservableList<PieChart.Data> pizzaDados = FXCollections.observableArrayList();
        BigDecimal somaOutros = BigDecimal.ZERO;
        List<String> nomesLegenda = new ArrayList<>();
        List<Double> porcentagensLegenda = new ArrayList<>();

        for (int i = 0; i < lista.size(); i++) {
            ResumoRanking item = lista.get(i);
            if (item.getValorDecimal().compareTo(BigDecimal.ZERO) == 0) continue;

            if (i < 5) {
                double porcentagem = (item.getValorDecimal().doubleValue() / somaTotal.doubleValue()) * 100;
                pizzaDados.add(new PieChart.Data(item.getNome(), item.getValorDecimal().doubleValue()));
                nomesLegenda.add(item.getNome());
                porcentagensLegenda.add(porcentagem);
            } else {
                somaOutros = somaOutros.add(item.getValorDecimal());
            }
        }

        if (somaOutros.compareTo(BigDecimal.ZERO) > 0) {
            double porcentagemOutros = (somaOutros.doubleValue() / somaTotal.doubleValue()) * 100;
            pizzaDados.add(new PieChart.Data("Outros", somaOutros.doubleValue()));
            nomesLegenda.add("Outros");
            porcentagensLegenda.add(porcentagemOutros);
        }

        grafico.setData(pizzaDados);

        for (int i = 0; i < pizzaDados.size(); i++) {
            String corHex = CORES_GRAFICO[i % CORES_GRAFICO.length];

            if (pizzaDados.get(i).getNode() != null) {
                pizzaDados.get(i).getNode().setStyle("-fx-pie-color: " + corHex + ";");
            }

            HBox linha = new HBox(15);
            linha.setAlignment(Pos.CENTER_LEFT);
            Circle bolinhaCor = new Circle(6, Color.web(corHex));
            Label lblNome = new Label(nomesLegenda.get(i) + ":");
            lblNome.setStyle("-fx-font-size: 15px; -fx-text-fill: #666666; -fx-font-weight: normal;");

            Region mola = new Region();
            HBox.setHgrow(mola, Priority.ALWAYS);

            Label lblPorcentagem = new Label(String.format("%.1f%%", porcentagensLegenda.get(i)));
            lblPorcentagem.setStyle("-fx-font-size: 16px; -fx-text-fill: #333333; -fx-font-weight: bold;");

            linha.getChildren().addAll(bolinhaCor, lblNome, mola, lblPorcentagem);
            boxLegenda.getChildren().add(linha);
        }
    }

    private void processarGraficoEvolucaoGeral(List<Compra> compras, List<Venda> vendas, BarChart<String, Number> grafico, CategoryAxis eixoX) {
        grafico.setAnimated(false);
        eixoX.setAnimated(false);
        grafico.getData().clear();

        String[] nomesMeses = {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
        eixoX.setCategories(FXCollections.observableArrayList(nomesMeses));
        eixoX.setTickLabelFill(Color.web("#333333"));
        eixoX.setTickLabelFont(Font.font("Inter", javafx.scene.text.FontWeight.BOLD, 14));

        NumberAxis eixoY = (NumberAxis) grafico.getYAxis();
        eixoY.setTickLabelFill(Color.web("#333333"));
        eixoY.setTickLabelFormatter(new NumberAxis.DefaultFormatter(eixoY, "R$ ", null));

        XYChart.Series<String, Number> serie = new XYChart.Series<>();
        BigDecimal[] valoresMes = new BigDecimal[12];
        Arrays.fill(valoresMes, BigDecimal.ZERO);

        if (compras != null) {
            for (Compra c : compras) {
                int mes = c.getDataTransacao().getMonthValue() - 1;
                valoresMes[mes] = valoresMes[mes].add(c.getValorTotal());
            }
        } else if (vendas != null) {
            for (Venda v : vendas) {
                int mes = v.getDataTransacao().getMonthValue() - 1;
                valoresMes[mes] = valoresMes[mes].add(v.getValorTotal());
            }
        }

        double maxValor = 0;
        for (BigDecimal valor : valoresMes) {
            if (valor.doubleValue() > maxValor) {
                maxValor = valor.doubleValue();
            }
        }

        if (maxValor > 0) {
            eixoY.setAutoRanging(false);
            eixoY.setLowerBound(0);
            eixoY.setUpperBound(maxValor * 1.20);
            double tickUnit = maxValor / 5;
            tickUnit = Math.ceil(tickUnit / 100) * 100;
            if (tickUnit == 0) tickUnit = 10;
            eixoY.setTickUnit(tickUnit);
        } else {
            eixoY.setAutoRanging(true);
        }

        NumberFormat formMoeda = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));

        for (int i = 0; i < 12; i++) {
            double valor = valoresMes[i].doubleValue();
            XYChart.Data<String, Number> data = new XYChart.Data<>(nomesMeses[i], valor);
            serie.getData().add(data);

            data.nodeProperty().addListener((obs, oldNode, newNode) -> {
                if (newNode != null) {
                    Tooltip tooltip = new Tooltip("Valor " + formMoeda.format(valor));
                    tooltip.setShowDelay(Duration.ZERO);
                    tooltip.setHideDelay(Duration.ZERO);
                    tooltip.getStyleClass().add("tooltip-grafico");
                    Tooltip.install(newNode, tooltip);
                    newNode.setOnMouseEntered(e -> newNode.setStyle("-fx-opacity: 0.7; -fx-cursor: hand;"));
                    newNode.setOnMouseExited(e -> newNode.setStyle("-fx-opacity: 1.0;"));
                    adicionarLabelNoTopo(data);
                }
            });
        }
        grafico.getData().add(serie);
    }

    private void adicionarLabelNoTopo(XYChart.Data<String, Number> data) {
        javafx.scene.Node barra = data.getNode();
        String textoValor = String.format(new Locale("pt", "BR"), "%,.2f", data.getYValue().doubleValue());
        javafx.scene.text.Text label = new javafx.scene.text.Text(textoValor);
        label.setStyle("-fx-font-size: 13px; -fx-font-weight: 900; -fx-fill: #000000;");

        barra.parentProperty().addListener((obs, oldP, newP) -> {
            if (newP instanceof javafx.scene.Group group && !group.getChildren().contains(label)) {
                group.getChildren().add(label);
            }
        });
        barra.boundsInParentProperty().addListener((obs, oldB, newB) -> {
            label.setLayoutX(newB.getMinX() + (newB.getWidth() / 2) - (label.getLayoutBounds().getWidth() / 2));
            label.setLayoutY(newB.getMinY() - 8);
        });
    }

    private void configurarTabelas() {
        configurarColunasTabela(colFornNome, colFornPedidos, colFornValor);
        configurarColunasTabela(colProdNome, colProdPedidos, colProdValor);

        configurarColunasTabela(colTipoVendaNome, colTipoVendaPedidos, colTipoVendaValor);
        configurarColunasTabela(colMatVendaNome, colMatVendaPedidos, colMatVendaValor);
        configurarColunasTabela(colProdVendaNome, colProdVendaPedidos, colProdVendaValor);
        configurarColunasTabela(colServVendaNome, colServVendaPedidos, colServVendaValor);
        configurarColunasTabela(colCliNome, colCliPedidos, colCliValor);

        if(colCliId != null) colCliId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if(colCliTipo != null) colCliTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        if(colForId != null) colForId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if(colForTipo != null) colForTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        if(colMatVendaId != null) colMatVendaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if(colProdVendaId != null) colProdVendaId.setCellValueFactory(new PropertyValueFactory<>("id"));
        if(colServVendaId != null) colServVendaId.setCellValueFactory(new PropertyValueFactory<>("id"));
    }

    private void configurarColunasTabela(TableColumn<ResumoRanking, String> colNome, TableColumn<ResumoRanking, Integer> colPedidos, TableColumn<ResumoRanking, String> colValor) {
        if(colNome != null) colNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        if(colPedidos != null) colPedidos.setCellValueFactory(new PropertyValueFactory<>("pedidos"));
        if(colValor != null) colValor.setCellValueFactory(new PropertyValueFactory<>("valorFormatado"));

        if (colValor != null) {
            colValor.setComparator((o1, o2) -> {
                try {
                    String cleanO1 = o1.replace("R$ ", "").replace(".", "").replace(",", ".");
                    String cleanO2 = o2.replace("R$ ", "").replace(".", "").replace(",", ".");
                    return new BigDecimal(cleanO1).compareTo(new BigDecimal(cleanO2));
                } catch (Exception e) {
                    return o1.compareTo(o2);
                }
            });
        }
    }

    public void configurarCss(){
        URL cssFC = getClass().getResource("/com/gerenciador/css/formularios.css");
        URL cssTC = getClass().getResource("/com/gerenciador/css/tabelas.css");
        if (cssFC != null) pane.getStylesheets().add(cssFC.toExternalForm());
        if (cssTC != null) pane.getStylesheets().add(cssTC.toExternalForm());
    }

    public static class ResumoRanking {
        private String nome;
        private String id = "-";
        private String tipo = "-";
        private int pedidos = 0;
        private BigDecimal valorTotal = BigDecimal.ZERO;

        public ResumoRanking(String nome) { this.nome = nome; }

        public void adicionar(BigDecimal valor) {
            this.pedidos++;
            this.valorTotal = this.valorTotal.add(valor);
        }

        public String getNome() { return nome; }
        public String getId() { return id; }
        public void setId(String id) { this.id = id; }
        public String getTipo() { return tipo; }
        public void setTipo(String tipo) { this.tipo = tipo; }
        public int getPedidos() { return pedidos; }
        public BigDecimal getValorDecimal() { return valorTotal; }
        public String getValorFormatado() { return String.format("R$ %,.2f", valorTotal); }
    }
}