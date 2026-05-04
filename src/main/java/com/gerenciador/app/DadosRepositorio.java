package com.gerenciador.app;

import com.gerenciador.dao.*;
import com.gerenciador.model.cliente.Cliente;
import com.gerenciador.model.comercializavel.*;
import com.gerenciador.model.fornecedor.Fornecedor;
import com.gerenciador.model.transacao.Compra;
import com.gerenciador.model.transacao.Venda;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class DadosRepositorio {

    private static final ObservableList<Cliente> clientes = FXCollections.observableArrayList();
    private static final ObservableList<Fornecedor> fornecedores = FXCollections.observableArrayList();
    private static final ObservableList<MateriaPrima> materiasPrimas = FXCollections.observableArrayList();
    private static final ObservableList<Comercializavel> comercializaveis = FXCollections.observableArrayList();
    private static final ObservableList<Compra> compras = FXCollections.observableArrayList();
    private static final ObservableList<Venda> vendas = FXCollections.observableArrayList();

    private static final ClienteDAO clienteDAO = new ClienteDAO();
    private static final FornecedorDAO fornecedorDAO = new FornecedorDAO();
    private static final MateriaPrimaDAO materiaPrimaDAO = new MateriaPrimaDAO();
    private static final ServicoDAO servicoDAO = new ServicoDAO();
    private static final ProdutoDAO produtoDAO = new ProdutoDAO();
    private static final CompraDAO compraDAO = new CompraDAO();
    private static final VendaDAO vendaDAO = new VendaDAO();

    public static void carregarDadosDoBanco() {
        try {
            clientes.setAll(clienteDAO.listarTodos());
            fornecedores.setAll(fornecedorDAO.listarTodos());
            materiasPrimas.setAll(materiaPrimaDAO.listarTodos());
            comercializaveis.addAll(servicoDAO.listarTodos());
            comercializaveis.addAll(produtoDAO.listarTodos());
            compras.setAll(compraDAO.listarTodos());
            vendas.setAll(vendaDAO.listarTodos());
            System.out.println("Dados carregados do SQLite com sucesso!");
        } catch (Exception e) {
            System.err.println("Erro ao carregar dados: " + e.getMessage());
        }
    }

    public static ObservableList<Cliente> getCliente() { return clientes; }
    public static ObservableList<Fornecedor> getFornecedores() { return fornecedores; }
    public static ObservableList<MateriaPrima> getMateriasPrimas() { return materiasPrimas; }
    public static ObservableList<Comercializavel> getComercializaveis() { return comercializaveis; }
    public static ObservableList<Compra> getCompras() { return compras; }
    public static ObservableList<Venda> getVendas() { return vendas; }

    public static void adicionarCliente(Cliente c) {
        try {
            // Tenta salvar no banco. Se falhar (ex: CPF repetido), vai pro catch.
            clienteDAO.salvar(c);
            // Só adiciona na lista visual se o banco aceitou.
            clientes.add(c);
        } catch (Exception e) {
            e.printStackTrace();
            // RE-LANÇA o erro para o Controller capturar e mostrar na UI
            throw new RuntimeException("Erro ao salvar Cliente: " + e.getMessage());
        }
    }
    public static void removerCliente(Cliente cliente) {
        try {
            List<Venda> vendasDoCliente = vendas.stream().filter(v -> v.getCliente().getId().equals(cliente.getId())).toList();
            for (Venda v : vendasDoCliente) {
                apagarArquivoFisico(v.getNomeComprovante(), "Vendas");
                apagarArquivoFisico(v.getNomeNotaFiscal(), "Vendas");
                apagarArquivoFisico(v.getNomeArquivoVenda(), "Vendas");
                vendas.remove(v);
            }
            clienteDAO.deletar(cliente.getId());
            clientes.remove(cliente);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void adicionarMateriaPrima(MateriaPrima mp) {
        try { materiaPrimaDAO.salvar(mp); materiasPrimas.add(mp); } catch (Exception e) { e.printStackTrace(); }
    }

    public static void removerMateriaPrima(MateriaPrima mp) {
        try { materiaPrimaDAO.deletar(mp.getId()); materiasPrimas.remove(mp); } catch (Exception e) { e.printStackTrace(); }
    }

    public static void adicionarFornecedor(Fornecedor f) {
        try {
            fornecedorDAO.salvar(f);
            fornecedores.add(f);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Erro ao salvar Fornecedor: " + e.getMessage());
        }
    }

    public static void removerFornecedor(Fornecedor f) {
        try {
            List<Compra> comprasDoFornecedor = compras.stream().filter(c -> c.getFornecedor().getId().equals(f.getId())).toList();
            for (Compra c : comprasDoFornecedor) {
                apagarArquivoFisico(c.getNomeComprovante(), "Compras");
                apagarArquivoFisico(c.getNomeNotaFiscal(), "Compras");
                compras.remove(c);
            }
            fornecedorDAO.deletar(f.getId());
            fornecedores.remove(f);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void adicionarCompra(Compra c) {
        try {
            compraDAO.salvar(c);
            compras.add(c);

            // Apenas salva no banco o estoque que já foi alterado na memória
            for (Compra.ItemCompra item : c.getItens()) {
                MateriaPrima mpLista = materiasPrimas.stream()
                        .filter(m -> m.getId().equals(item.getMateriaPrima().getId()))
                        .findFirst().orElse(item.getMateriaPrima());

                materiaPrimaDAO.atualizar(mpLista); // Salva sem duplicar a conta!
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removerCompra(Compra c) {
        try {
            // 1. Reverter o estoque das matérias-primas antes de excluir
            for (Compra.ItemCompra item : c.getItens()) {
                MateriaPrima mpLista = materiasPrimas.stream()
                        .filter(m -> m.getId().equals(item.getMateriaPrima().getId()))
                        .findFirst().orElse(item.getMateriaPrima());

                // Subtrai do estoque e do total comprado o que foi cancelado
                mpLista.setEstoqueAtual(mpLista.getEstoqueAtual().subtract(item.getQuantidade()));
                mpLista.setTotalComprado(mpLista.getTotalComprado().subtract(item.getQuantidade()));

                // Atualiza a matéria-prima no banco de dados
                materiaPrimaDAO.atualizar(mpLista);
            }

            // 2. Remove a compra
            compraDAO.deletar(c.getId());
            compras.remove(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void adicionarVenda(Venda v) {
        try {
            vendaDAO.salvar(v);
            vendas.add(v);

            for (Venda.ItemVenda item : v.getItens()) {
                // 1. Persiste a atualização do volume vendido do item vendido
                atualizarComercializavel(item.getItem());

                // 2. Mantém a lógica existente para baixar estoque de Matérias-Primas
                if (item.getItem() instanceof MateriaPrima mp) {
                    materiaPrimaDAO.atualizar(mp);
                } else if (item.getItem() instanceof Produto && item.getComposicao() != null) {
                    for (MateriaPrima mp : item.getComposicao().ingredientes().keySet()) {
                        materiaPrimaDAO.atualizar(mp);
                    }
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    public static void removerVenda(Venda v) {
        try {
            for (Venda.ItemVenda item : v.getItens()) {
                Comercializavel comercializavel = item.getItem();
                BigDecimal quantidade = item.getQuantidade();

                // 1. Lógica para Serviços ou Produtos
                if (comercializavel instanceof Servico || comercializavel instanceof Produto) {
                    // Diminuir o volume total vendido do item
                    comercializavel.setTotalVendido(comercializavel.getTotalVendido().subtract(quantidade));

                    // Se for um Produto com composição, devolve as matérias-primas ao estoque
                    if (comercializavel instanceof Produto && item.getComposicao() != null) {
                        for (Map.Entry<MateriaPrima, BigDecimal> entry : item.getComposicao().ingredientes().entrySet()) {
                            MateriaPrima mpIngrediente = entry.getKey();
                            BigDecimal qtdUsada = entry.getValue();

                            // Busca a referência correta na lista para garantir atualização em memória
                            MateriaPrima mpLista = materiasPrimas.stream()
                                    .filter(m -> m.getId().equals(mpIngrediente.getId()))
                                    .findFirst().orElse(mpIngrediente);

                            mpLista.setEstoqueAtual(mpLista.getEstoqueAtual().add(qtdUsada));
                            materiaPrimaDAO.atualizar(mpLista);
                        }
                    }
                    // Salva a alteração do volume vendido do Serviço ou Produto
                    atualizarComercializavel(comercializavel);
                }

                // 2. Lógica para Venda Direta de Matéria-Prima
                else if (comercializavel instanceof MateriaPrima mp) {
                    // Busca a referência na lista central
                    MateriaPrima mpLista = materiasPrimas.stream()
                            .filter(m -> m.getId().equals(mp.getId()))
                            .findFirst().orElse(mp);

                    // Diminui o total vendido e devolve a quantidade ao estoque
                    mpLista.setTotalVendido(mpLista.getTotalVendido().subtract(quantidade));
                    mpLista.setEstoqueAtual(mpLista.getEstoqueAtual().add(quantidade));

                    // Persiste a matéria-prima atualizada
                    materiaPrimaDAO.atualizar(mpLista);
                }
            }

            // 3. Remove a venda do banco e da lista observável[cite: 12]
            vendaDAO.deletar(v.getId());
            vendas.remove(v);

        } catch (Exception e) {
            System.err.println("Erro ao remover venda e estornar valores: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void adicionarProduto(Produto p) {
        try { produtoDAO.salvar(p); comercializaveis.add(p); } catch (Exception e) { e.printStackTrace(); }
    }

    public static void removerProdutos(Produto p) {
        try { produtoDAO.deletar(p.getId()); comercializaveis.remove(p); } catch (Exception e) { e.printStackTrace(); }
    }

    public static void adicionarServico(Servico v) {
        try { servicoDAO.salvar(v); comercializaveis.add(v); } catch (Exception e) { e.printStackTrace(); }
    }

    public static void removerServico(Servico v) {
        try { servicoDAO.deletar(v.getId()); comercializaveis.remove(v); } catch (Exception e) { e.printStackTrace(); }
    }

    public static ObservableList<Integer> getAnos() {
        ObservableList<Integer> anos = FXCollections.observableArrayList();
        for (Compra c : compras) { int ano = c.getDataTransacao().getYear(); if (!anos.contains(ano)) anos.add(ano); }
        for (Venda v : vendas) { int ano = v.getDataTransacao().getYear(); if (!anos.contains(ano)) anos.add(ano); }
        FXCollections.sort(anos);
        return anos;
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }

    public static ObservableList<Compra> getComprasFiltradas(String periodo, Integer ano, String mesStr) {
        ObservableList<Compra> filtradas = FXCollections.observableArrayList();
        for (Compra c : compras) {
            LocalDate data = c.getDataTransacao();
            if ("Total".equals(periodo)) filtradas.add(c);
            else if ("Anual".equals(periodo) && ano != null && data.getYear() == ano) filtradas.add(c);
            else if ("Mensal".equals(periodo) && ano != null && mesStr != null) {
                String mes = capitalize(data.getMonth().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR")));
                if (data.getYear() == ano && mes.equals(mesStr)) filtradas.add(c);
            }
        }
        return filtradas;
    }

    public static void atualizarCliente(Cliente cliente) { try { clienteDAO.atualizar(cliente); } catch (Exception e) { e.printStackTrace(); } }
    public static void atualizarFornecedor(Fornecedor fornecedor) { try { fornecedorDAO.atualizar(fornecedor); } catch (Exception e) { e.printStackTrace(); } }
    public static void atualizarMateriaPrima(MateriaPrima mp) { try { materiaPrimaDAO.atualizar(mp); } catch (Exception e) { e.printStackTrace(); } }
    public static void atualizarComercializavel(Comercializavel item) {
        try {
            if (item instanceof Produto) produtoDAO.atualizar((Produto) item);
            else if (item instanceof Servico) servicoDAO.atualizar((Servico) item);
        } catch (Exception e) { e.printStackTrace(); }
    }
    public static void atualizarCompra(Compra compra) { try { compraDAO.atualizar(compra); } catch (Exception e) { e.printStackTrace(); } }
    public static void atualizarVenda(Venda venda) { try { vendaDAO.atualizar(venda); } catch (Exception e) { e.printStackTrace(); } }

    public static ObservableList<String> getMesesComDados(int ano) {
        Set<Integer> mesesNumeros = new TreeSet<>();
        for (Compra c : compras) { if (c.getDataTransacao().getYear() == ano) mesesNumeros.add(c.getDataTransacao().getMonthValue()); }
        for (Venda v : vendas) { if (v.getDataTransacao().getYear() == ano) mesesNumeros.add(v.getDataTransacao().getMonthValue()); }
        ObservableList<String> mesesNomes = FXCollections.observableArrayList();
        for (Integer mes : mesesNumeros) {
            String nomeMes = LocalDate.of(ano, mes, 1).getMonth().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR"));
            mesesNomes.add(capitalize(nomeMes));
        }
        return mesesNomes;
    }

    public static ObservableList<Venda> getVendasFiltradas(String periodo, Integer ano, String mesStr) {
        ObservableList<Venda> filtradas = FXCollections.observableArrayList();
        for (Venda v : vendas) {
            LocalDate data = v.getDataTransacao();
            if ("Total".equals(periodo)) filtradas.add(v);
            else if ("Anual".equals(periodo) && ano != null && data.getYear() == ano) filtradas.add(v);
            else if ("Mensal".equals(periodo) && ano != null && mesStr != null) {
                String mesVenda = capitalize(data.getMonth().getDisplayName(TextStyle.FULL, Locale.of("pt", "BR")));
                if (data.getYear() == ano && mesVenda.equals(mesStr)) filtradas.add(v);
            }
        }
        return filtradas;
    }

    private static void apagarArquivoFisico(String nomeArquivo, String nomePasta) {
        if (nomeArquivo != null && !nomeArquivo.isEmpty()) {
            String os = System.getProperty("os.name").toLowerCase();
            String diretorioApp = os.contains("win")
                    ? System.getenv("APPDATA") + File.separator + "GerenciadorApp"
                    : System.getProperty("user.home") + File.separator + "GerenciadorApp";
            String caminhoArquivo = diretorioApp + File.separator + "PDF" + File.separator + nomePasta + File.separator + nomeArquivo;
            try { Files.deleteIfExists(Paths.get(caminhoArquivo)); System.out.println("PDF órfão apagado: " + nomeArquivo); }
            catch (Exception e) { System.err.println("Erro ao tentar apagar PDF: " + e.getMessage()); }
        }
    }
}