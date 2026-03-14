package com.gerenciador.dao;

import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.fornecedor.Fornecedor;
import com.gerenciador.model.transacao.Compra;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import com.gerenciador.app.DatabaseSetup;

public class CompraDAO {

    public void salvar(Compra compra) throws SQLException {
        String sql = "INSERT INTO compras (id_fornecedor, valor_total, data_compra, data_limite, data_pagamento, pago, observacao, nome_comprovante, nome_nota_fiscal) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, compra.getFornecedor().getId());
            stmt.setDouble(2, compra.getValorTotal().doubleValue());
            stmt.setString(3, compra.getDataTransacao().toString());
            stmt.setString(4, compra.getDataLimite().toString());

            if (compra.getDataPagamento() != null) stmt.setString(5, compra.getDataPagamento().toString());
            else stmt.setNull(5, Types.VARCHAR);

            stmt.setInt(6, compra.getEstaPago() ? 1 : 0);
            stmt.setString(7, compra.getObservacao());
            stmt.setString(8, compra.getNomeComprovante());
            stmt.setString(9, compra.getNomeNotaFiscal());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) compra.setId(rs.getInt(1));
            }
            salvarItens(compra, conn);
        }
    }

    private void salvarItens(Compra compra, Connection conn) throws SQLException {
        String sql = "INSERT INTO compra_itens (id_compra, id_materia_prima, quantidade, valor_unidade, valor_total) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            for (Compra.ItemCompra item : compra.getItens()) {
                stmt.setInt(1, compra.getId());
                stmt.setInt(2, item.getMateriaPrima().getId());
                stmt.setDouble(3, item.getQuantidade().doubleValue());
                stmt.setDouble(4, item.getValorUnidade().doubleValue());
                stmt.setDouble(5, item.getValorTotal().doubleValue());
                stmt.executeUpdate();
            }
        }
    }

    public void atualizar(Compra compra) throws SQLException {
        String sql = "UPDATE compras SET id_fornecedor=?, valor_total=?, data_compra=?, data_limite=?, data_pagamento=?, pago=?, observacao=?, nome_comprovante=?, nome_nota_fiscal=? WHERE id=?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, compra.getFornecedor().getId());
            stmt.setDouble(2, compra.getValorTotal().doubleValue());
            stmt.setString(3, compra.getDataTransacao().toString());
            stmt.setString(4, compra.getDataLimite().toString());

            if (compra.getDataPagamento() != null) stmt.setString(5, compra.getDataPagamento().toString());
            else stmt.setNull(5, Types.VARCHAR);

            stmt.setInt(6, compra.getEstaPago() ? 1 : 0);
            stmt.setString(7, compra.getObservacao());
            stmt.setString(8, compra.getNomeComprovante());
            stmt.setString(9, compra.getNomeNotaFiscal());
            stmt.setInt(10, compra.getId());

            stmt.executeUpdate();

            // Recriar itens
            try (PreparedStatement deleteStmt = conn.prepareStatement("DELETE FROM compra_itens WHERE id_compra=?")) {
                deleteStmt.setInt(1, compra.getId());
                deleteStmt.executeUpdate();
            }
            salvarItens(compra, conn);
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM compras WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate(); // Cascata deletará os compra_itens
        }
    }

    public List<Compra> listarTodos() throws SQLException {
        List<Compra> lista = new ArrayList<>();
        String sql = "SELECT * FROM compras";
        FornecedorDAO fornecedorDAO = new FornecedorDAO();
        MateriaPrimaDAO materiaPrimaDAO = new MateriaPrimaDAO();

        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Fornecedor f = fornecedorDAO.buscarPorId(rs.getInt("id_fornecedor"));
                int idCompra = rs.getInt("id");

                LocalDate dataCompra = LocalDate.parse(rs.getString("data_compra"));
                LocalDate dataLimite = LocalDate.parse(rs.getString("data_limite"));
                String dataPagamentoStr = rs.getString("data_pagamento");
                LocalDate dataPagamento = dataPagamentoStr != null ? LocalDate.parse(dataPagamentoStr) : null;

                List<Compra.ItemCompra> itens = buscarItensDaCompra(idCompra, conn, materiaPrimaDAO);

                Compra compra = new Compra(
                        dataCompra, f, itens, rs.getInt("pago") == 1, dataLimite, dataPagamento, null, null, rs.getString("observacao")
                );

                compra.setId(idCompra);
                compra.setValorTotal(BigDecimal.valueOf(rs.getDouble("valor_total")));
                compra.setNomeComprovante(rs.getString("nome_comprovante"));
                compra.setNomeNotaFiscal(rs.getString("nome_nota_fiscal"));
                lista.add(compra);
            }
        }
        return lista;
    }

    private List<Compra.ItemCompra> buscarItensDaCompra(int idCompra, Connection conn, MateriaPrimaDAO mpDAO) throws SQLException {
        List<Compra.ItemCompra> itens = new ArrayList<>();
        String sql = "SELECT * FROM compra_itens WHERE id_compra = ?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idCompra);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    MateriaPrima mp = mpDAO.buscarPorId(rs.getInt("id_materia_prima"));
                    itens.add(new Compra.ItemCompra(mp, BigDecimal.valueOf(rs.getDouble("quantidade")), BigDecimal.valueOf(rs.getDouble("valor_unidade"))));
                }
            }
        }
        return itens;
    }
}