package com.gerenciador.dao;

import com.gerenciador.model.comercializavel.MateriaPrima;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.gerenciador.app.DatabaseSetup;
import java.sql.Connection;
import java.sql.SQLException;
public class MateriaPrimaDAO {

    public void salvar(MateriaPrima mp) throws SQLException {
        String sql = "INSERT INTO materias_primas (nome, unidade, estoque_atual, total_comprado, total_vendido) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, mp.getNome());
            stmt.setString(2, mp.getUnidade());
            stmt.setDouble(3, mp.getEstoqueAtual().doubleValue());
            stmt.setDouble(4, mp.getTotalComprado().doubleValue());
            stmt.setDouble(5, mp.getTotalVendido().doubleValue());
            stmt.executeUpdate();

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) mp.setId(generatedKeys.getInt(1));
            }
        }
    }

    public List<MateriaPrima> listarTodos() throws SQLException {
        List<MateriaPrima> lista = new ArrayList<>();
        String sql = "SELECT * FROM materias_primas";
        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                MateriaPrima mp = new MateriaPrima(rs.getString("nome"), rs.getString("unidade"));
                mp.setId(rs.getInt("id"));
                mp.setEstoqueAtual(BigDecimal.valueOf(rs.getDouble("estoque_atual")));
                mp.setTotalComprado(BigDecimal.valueOf(rs.getDouble("total_comprado")));
                mp.setTotalVendido(BigDecimal.valueOf(rs.getDouble("total_vendido")));
                lista.add(mp);
            }
        }
        return lista;
    }

    public void atualizar(MateriaPrima mp) throws SQLException {
        String sql = "UPDATE materias_primas SET nome = ?, unidade = ?, estoque_atual = ?, total_comprado = ?, total_vendido = ? WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, mp.getNome());
            stmt.setString(2, mp.getUnidade());
            stmt.setDouble(3, mp.getEstoqueAtual().doubleValue());
            stmt.setDouble(4, mp.getTotalComprado().doubleValue());
            stmt.setDouble(5, mp.getTotalVendido().doubleValue());
            stmt.setInt(6, mp.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM materias_primas WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }
    public MateriaPrima buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM materias_primas WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    MateriaPrima mp = new MateriaPrima(rs.getString("nome"), rs.getString("unidade"));
                    mp.setId(rs.getInt("id"));
                    mp.setEstoqueAtual(BigDecimal.valueOf(rs.getDouble("estoque_atual")));
                    mp.setTotalComprado(BigDecimal.valueOf(rs.getDouble("total_comprado")));
                    mp.setTotalVendido(BigDecimal.valueOf(rs.getDouble("total_vendido")));
                    return mp;
                }
            }
        }
        return null;
    }
}