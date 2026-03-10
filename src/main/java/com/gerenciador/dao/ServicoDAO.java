package com.gerenciador.dao;

import com.gerenciador.model.comercializavel.Servico;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.gerenciador.app.DatabaseSetup;
import java.sql.Connection;
import java.sql.SQLException;
public class ServicoDAO {

    public void salvar(Servico servico) throws SQLException {
        String sql = "INSERT INTO servicos (nome, total_vendido) VALUES (?, ?)";
        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, servico.getNome());
            stmt.setDouble(2, servico.getTotalVendido().doubleValue());
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) servico.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Servico servico) throws SQLException {
        String sql = "UPDATE servicos SET nome = ?, total_vendido = ? WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, servico.getNome());
            stmt.setDouble(2, servico.getTotalVendido().doubleValue());
            stmt.setInt(3, servico.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM servicos WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Servico> listarTodos() throws SQLException {
        List<Servico> lista = new ArrayList<>();
        String sql = "SELECT * FROM servicos";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Servico servico = new Servico(rs.getString("nome"));
                servico.setId(rs.getInt("id"));
                servico.setTotalVendido(BigDecimal.valueOf(rs.getDouble("total_vendido")));
                lista.add(servico);
            }
        }
        return lista;
    }

    public Servico buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM servicos WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Servico servico = new Servico(rs.getString("nome"));
                    servico.setId(rs.getInt("id"));
                    servico.setTotalVendido(BigDecimal.valueOf(rs.getDouble("total_vendido")));
                    return servico;
                }
            }
        }
        return null;
    }
}