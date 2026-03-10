package com.gerenciador.dao;

import com.gerenciador.model.fornecedor.Fornecedor;
import com.gerenciador.model.fornecedor.FornecedorPF;
import com.gerenciador.model.fornecedor.FornecedorPJ;
import com.gerenciador.model.pessoa.Endereco;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.gerenciador.app.DatabaseSetup;
import java.sql.Connection;
import java.sql.SQLException;
public class FornecedorDAO {

    public void salvar(Fornecedor fornecedor) throws SQLException {
        String sql = "INSERT INTO fornecedores (tipo_pessoa, nome, telefone, cidade, bairro, rua, numero_casa, documento, inscricao_estadual, razao_social, nome_responsavel) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            boolean isPF = fornecedor instanceof FornecedorPF;
            stmt.setString(1, isPF ? "PF" : "PJ");
            stmt.setString(2, fornecedor.getNomePrincipal());
            stmt.setString(3, fornecedor.getTelefone());
            stmt.setString(4, fornecedor.getEndereco().getCidade());
            stmt.setString(5, fornecedor.getEndereco().getBairro());
            stmt.setString(6, fornecedor.getEndereco().getRua());
            stmt.setString(7, fornecedor.getEndereco().getnumeroCasa());
            stmt.setString(8, fornecedor.getDocumento());

            if (isPF) {
                stmt.setNull(9, Types.VARCHAR);
                stmt.setNull(10, Types.VARCHAR);
                stmt.setNull(11, Types.VARCHAR);
            } else {
                FornecedorPJ pj = (FornecedorPJ) fornecedor;
                stmt.setString(9, pj.getInscricaoEstadual());
                stmt.setString(10, pj.getRazaoSocial());
                stmt.setString(11, pj.getNomeResponsavel());
            }

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) fornecedor.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Fornecedor fornecedor) throws SQLException {
        String sql = "UPDATE fornecedores SET nome=?, telefone=?, cidade=?, bairro=?, rua=?, numero_casa=?, documento=?, inscricao_estadual=?, razao_social=?, nome_responsavel=? WHERE id=?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, fornecedor.getNomePrincipal());
            stmt.setString(2, fornecedor.getTelefone());
            stmt.setString(3, fornecedor.getEndereco().getCidade());
            stmt.setString(4, fornecedor.getEndereco().getBairro());
            stmt.setString(5, fornecedor.getEndereco().getRua());
            stmt.setString(6, fornecedor.getEndereco().getnumeroCasa());
            stmt.setString(7, fornecedor.getDocumento());

            if (fornecedor instanceof FornecedorPJ pj) {
                stmt.setString(8, pj.getInscricaoEstadual());
                stmt.setString(9, pj.getRazaoSocial());
                stmt.setString(10, pj.getNomeResponsavel());
            } else {
                stmt.setNull(8, Types.VARCHAR); stmt.setNull(9, Types.VARCHAR); stmt.setNull(10, Types.VARCHAR);
            }
            stmt.setInt(11, fornecedor.getId());
            stmt.executeUpdate();
        }
    }

    public void deletar(int id) throws SQLException {
        String sql = "DELETE FROM fornecedores WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public List<Fornecedor> listarTodos() throws SQLException {
        List<Fornecedor> lista = new ArrayList<>();
        String sql = "SELECT * FROM fornecedores";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                lista.add(montarFornecedor(rs));
            }
        }
        return lista;
    }

    public Fornecedor buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM fornecedores WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return montarFornecedor(rs);
            }
        }
        return null;
    }


    private Fornecedor montarFornecedor(ResultSet rs) throws SQLException {
        Endereco end = new Endereco(rs.getString("cidade"), rs.getString("bairro"), rs.getString("rua"), rs.getString("numero_casa"));
        Fornecedor fornecedor;
        if ("PF".equals(rs.getString("tipo_pessoa"))) {
            fornecedor = new FornecedorPF(rs.getString("nome"), rs.getString("documento"), rs.getString("telefone"), end);
        } else {
            fornecedor = new FornecedorPJ(rs.getString("nome"), rs.getString("telefone"), end, rs.getString("documento"), rs.getString("nome_responsavel"), rs.getString("inscricao_estadual"), rs.getString("razao_social"));
        }
        fornecedor.setId(rs.getInt("id"));
        return fornecedor;
    }
}