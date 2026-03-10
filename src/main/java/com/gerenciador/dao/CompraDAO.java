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
import java.sql.Connection;
import java.sql.SQLException;
public class CompraDAO {

    public void salvar(Compra compra) throws SQLException {
        String sql = "INSERT INTO compras (id_fornecedor, id_materia_prima, quantidade, valor_unidade, valor_total, data_compra, data_limite, data_pagamento, pago, observacao, nome_pdf) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, compra.getFornecedor().getId());
            stmt.setInt(2, compra.getProduto().getId());
            stmt.setDouble(3, compra.getQuantidade().doubleValue());
            stmt.setDouble(4, compra.getValorUnidade().doubleValue());
            stmt.setDouble(5, compra.getValorTotal().doubleValue());
            stmt.setString(6, compra.getDataTransacao().toString());
            stmt.setString(7, compra.getDataLimite().toString());

            if (compra.getDataPagamento() != null) {
                stmt.setString(8, compra.getDataPagamento().toString());
            } else {
                stmt.setNull(8, Types.VARCHAR);
            }

            stmt.setInt(9, compra.getEstaPago() ? 1 : 0);
            stmt.setString(10, compra.getObservacao());
            stmt.setString(11, compra.getNomeArquivoPdf());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) compra.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Compra compra) throws SQLException {
        String sql = "UPDATE compras SET id_fornecedor=?, id_materia_prima=?, quantidade=?, valor_unidade=?, valor_total=?, data_compra=?, data_limite=?, data_pagamento=?, pago=?, observacao=?, nome_pdf=? WHERE id=?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, compra.getFornecedor().getId());
            stmt.setInt(2, compra.getProduto().getId());
            stmt.setDouble(3, compra.getQuantidade().doubleValue());
            stmt.setDouble(4, compra.getValorUnidade().doubleValue());
            stmt.setDouble(5, compra.getValorTotal().doubleValue());
            stmt.setString(6, compra.getDataTransacao().toString());
            stmt.setString(7, compra.getDataLimite().toString());

            if (compra.getDataPagamento() != null) stmt.setString(8, compra.getDataPagamento().toString());
            else stmt.setNull(8, Types.VARCHAR);

            stmt.setInt(9, compra.getEstaPago() ? 1 : 0);
            stmt.setString(10, compra.getObservacao());
            stmt.setString(11, compra.getNomeArquivoPdf());
            stmt.setInt(12, compra.getId());

            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM compras WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
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
                MateriaPrima mp = materiaPrimaDAO.buscarPorId(rs.getInt("id_materia_prima"));

                LocalDate dataCompra = LocalDate.parse(rs.getString("data_compra"));
                LocalDate dataLimite = LocalDate.parse(rs.getString("data_limite"));
                String dataPagamentoStr = rs.getString("data_pagamento");
                LocalDate dataPagamento = dataPagamentoStr != null ? LocalDate.parse(dataPagamentoStr) : null;

                Compra compra = new Compra(
                        dataCompra,
                        f,
                        mp,
                        BigDecimal.valueOf(rs.getDouble("quantidade")),
                        BigDecimal.valueOf(rs.getDouble("valor_unidade")),
                        rs.getInt("pago") == 1,
                        dataLimite,
                        dataPagamento,
                        null,
                        rs.getString("observacao")
                );

                compra.setId(rs.getInt("id"));
                compra.setValorTotal(BigDecimal.valueOf(rs.getDouble("valor_total")));
                compra.setNomeArquivoPdf(rs.getString("nome_pdf"));
                lista.add(compra);
            }
        }
        return lista;
    }
}