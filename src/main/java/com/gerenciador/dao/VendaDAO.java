package com.gerenciador.dao;

import com.gerenciador.model.cliente.Cliente;
import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.comercializavel.Produto;
import com.gerenciador.model.comercializavel.Servico;
import com.gerenciador.model.transacao.Venda;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.gerenciador.app.DatabaseSetup;

public class VendaDAO {

    public void salvar(Venda venda) throws SQLException {
        String sql = "INSERT INTO vendas (numero_venda, id_cliente, valor_total, data_venda, data_limite, data_pagamento, pago, observacao, nome_comprovante, nome_nota_fiscal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, venda.getNumeroVenda());
            stmt.setInt(2, venda.getCliente().getId());
            stmt.setDouble(3, venda.getValorTotal().doubleValue());
            stmt.setString(4, venda.getDataTransacao().toString());
            stmt.setString(5, venda.getDataLimite().toString());

            if (venda.getDataPagamento() != null) stmt.setString(6, venda.getDataPagamento().toString());
            else stmt.setNull(6, Types.VARCHAR);

            stmt.setInt(7, venda.getEstaPago() ? 1 : 0);
            stmt.setString(8, venda.getObservacao());
            stmt.setString(9, venda.getNomeComprovante());
            stmt.setString(10, venda.getNomeNotaFiscal());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) venda.setId(rs.getInt(1));
            }
            salvarItens(venda, conn);
        }
    }

    private void salvarItens(Venda venda, Connection conn) throws SQLException {
        String sql = "INSERT INTO venda_itens (id_venda, tipo_item, id_item, quantidade, valor_unidade, valor_total) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Venda.ItemVenda item : venda.getItens()) {
                stmt.setInt(1, venda.getId());
                if (item.getItem() instanceof Produto) stmt.setString(2, "PRODUTO");
                else if (item.getItem() instanceof Servico) stmt.setString(2, "SERVICO");
                else stmt.setString(2, "MATERIA_PRIMA");

                stmt.setInt(3, item.getItem().getId());
                stmt.setDouble(4, item.getQuantidade().doubleValue());
                stmt.setDouble(5, item.getValorUnidade().doubleValue());
                stmt.setDouble(6, item.getValorTotal().doubleValue());
                stmt.executeUpdate();
            }
        }
    }

    public void atualizar(Venda venda) throws SQLException {
        String sql = "UPDATE vendas SET numero_venda=?, id_cliente=?, valor_total=?, data_venda=?, data_limite=?, data_pagamento=?, pago=?, observacao=?, nome_comprovante=?, nome_nota_fiscal=? WHERE id=?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, venda.getNumeroVenda());
            stmt.setInt(2, venda.getCliente().getId());
            stmt.setDouble(3, venda.getValorTotal().doubleValue());
            stmt.setString(4, venda.getDataTransacao().toString());
            stmt.setString(5, venda.getDataLimite().toString());

            if (venda.getDataPagamento() != null) stmt.setString(6, venda.getDataPagamento().toString());
            else stmt.setNull(6, Types.VARCHAR);

            stmt.setInt(7, venda.getEstaPago() ? 1 : 0);
            stmt.setString(8, venda.getObservacao());
            stmt.setString(9, venda.getNomeComprovante());
            stmt.setString(10, venda.getNomeNotaFiscal());
            stmt.setInt(11, venda.getId());

            stmt.executeUpdate();

            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM venda_itens WHERE id_venda=?")) {
                deleteStmt.setInt(1, venda.getId());
                deleteStmt.executeUpdate();
            }
            salvarItens(venda, conn);
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM vendas WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Venda> listarTodos() throws SQLException {
        List<Venda> lista = new ArrayList<>();
        String sql = "SELECT * FROM vendas";

        ClienteDAO clienteDAO = new ClienteDAO();
        ProdutoDAO produtoDAO = new ProdutoDAO();
        ServicoDAO servicoDAO = new ServicoDAO();
        MateriaPrimaDAO materiaPrimaDAO = new MateriaPrimaDAO();

        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Cliente cliente = clienteDAO.buscarPorId(rs.getInt("id_cliente"));
                int idVenda = rs.getInt("id");
                int numVenda = rs.getInt("numero_venda");

                LocalDate dataVenda = LocalDate.parse(rs.getString("data_venda"));
                LocalDate dataLimite = LocalDate.parse(rs.getString("data_limite"));
                String dataPagamentoStr = rs.getString("data_pagamento");
                LocalDate dataPagamento = dataPagamentoStr != null ? LocalDate.parse(dataPagamentoStr) : null;

                List<Venda.ItemVenda> itens = buscarItensDaVenda(idVenda, conn, produtoDAO, servicoDAO, materiaPrimaDAO);

                Venda venda = new Venda(numVenda, dataVenda, cliente, itens, rs.getInt("pago") == 1, dataLimite, dataPagamento, null, null, rs.getString("observacao"));

                venda.setId(idVenda);
                venda.setValorTotal(BigDecimal.valueOf(rs.getDouble("valor_total")));
                venda.setNomeComprovante(rs.getString("nome_comprovante"));
                venda.setNomeNotaFiscal(rs.getString("nome_nota_fiscal"));
                lista.add(venda);
            }
        }
        return lista;
    }

    private List<Venda.ItemVenda> buscarItensDaVenda(int idVenda, Connection conn, ProdutoDAO prodDAO, ServicoDAO servDAO, MateriaPrimaDAO mpDAO) throws SQLException {
        List<Venda.ItemVenda> itens = new ArrayList<>();
        String sql = "SELECT * FROM venda_itens WHERE id_venda = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idVenda);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String tipoItem = rs.getString("tipo_item");
                    int idItem = rs.getInt("id_item");

                    com.gerenciador.model.comercializavel.Comercializavel item = null;
                    if ("PRODUTO".equals(tipoItem)) item = prodDAO.buscarPorId(idItem);
                    else if ("SERVICO".equals(tipoItem)) item = servDAO.buscarPorId(idItem);
                    else if ("MATERIA_PRIMA".equals(tipoItem)) item = mpDAO.buscarPorId(idItem);

                    com.gerenciador.model.transacao.ComposicaoVenda composicao = null;
                    if (item instanceof Produto produto) {
                        java.util.Map<MateriaPrima, BigDecimal> materiasUsadas = new java.util.HashMap<>();
                        for (MateriaPrima mp : produto.getMateriasPrimasNecessarias()) {
                            materiasUsadas.put(mp, BigDecimal.ONE);
                        }
                        composicao = new com.gerenciador.model.transacao.ComposicaoVenda(materiasUsadas);
                    }
                    itens.add(new Venda.ItemVenda(item, BigDecimal.valueOf(rs.getDouble("quantidade")), BigDecimal.valueOf(rs.getDouble("valor_unidade")), composicao));
                }
            }
        }
        return itens;
    }
}