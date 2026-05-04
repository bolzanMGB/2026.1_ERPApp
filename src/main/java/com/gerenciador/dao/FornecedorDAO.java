package com.gerenciador.dao;

import com.gerenciador.model.fornecedor.Fornecedor;
import com.gerenciador.model.fornecedor.FornecedorPF;
import com.gerenciador.model.fornecedor.FornecedorPJ;
import com.gerenciador.model.pessoa.Endereco;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.gerenciador.app.DatabaseSetup;

public class FornecedorDAO {

    public void salvar(Fornecedor fornecedor) throws SQLException {
        String sql = "INSERT INTO fornecedores (tipo_pessoa, nome, telefone, telefone2, observacao, cidade, bairro, rua, numero_casa, documento, inscricao_estadual, razao_social, nome_responsavel) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            configurarStatement(stmt, fornecedor);
            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) fornecedor.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Fornecedor fornecedor) throws SQLException {
        String sql = "UPDATE fornecedores SET tipo_pessoa=?, nome=?, telefone=?, telefone2=?, observacao=?, cidade=?, bairro=?, rua=?, numero_casa=?, documento=?, inscricao_estadual=?, razao_social=?, nome_responsavel=? WHERE id=?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

            configurarStatement(stmt, fornecedor);
            stmt.setInt(14, fornecedor.getId());
            stmt.executeUpdate();
        }
    }

    private void configurarStatement(PreparedStatement stmt, Fornecedor fornecedor) throws SQLException {
        boolean isPF = fornecedor instanceof FornecedorPF;

        stmt.setString(1, isPF ? "PF" : "PJ");
        stmt.setString(2, fornecedor.getNomePrincipal());
        stmt.setString(3, fornecedor.getTelefone());
        stmt.setString(4, fornecedor.getTelefone2());
        stmt.setString(5, fornecedor.getObservacao());
        stmt.setString(6, fornecedor.getEndereco().getCidade());
        stmt.setString(7, fornecedor.getEndereco().getBairro());
        stmt.setString(8, fornecedor.getEndereco().getRua());
        stmt.setString(9, fornecedor.getEndereco().getnumeroCasa());
        // Supondo que o seu objeto Fornecedor tenha o método getCnpj() ou getDocumento():
        if (fornecedor.getDocumento() == null || fornecedor.getDocumento().trim().isEmpty()) {
            stmt.setNull(10, java.sql.Types.VARCHAR);
        } else {
            stmt.setString(10, fornecedor.getDocumento());
        }

        if (isPF) {
            stmt.setNull(11, Types.VARCHAR);
            stmt.setNull(12, Types.VARCHAR);
            stmt.setNull(13, Types.VARCHAR);
        } else {
            FornecedorPJ pj = (FornecedorPJ) fornecedor;
            stmt.setString(11, pj.getInscricaoEstadual());
            stmt.setString(12, pj.getRazaoSocial());
            stmt.setString(13, pj.getNomeResponsavel());
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
            fornecedor = new FornecedorPF(rs.getString("nome"), rs.getString("documento"), rs.getString("telefone"), rs.getString("telefone2"), rs.getString("observacao"), end);
        } else {
            fornecedor = new FornecedorPJ(rs.getString("nome"), rs.getString("telefone"), rs.getString("telefone2"), rs.getString("observacao"), end, rs.getString("documento"), rs.getString("nome_responsavel"), rs.getString("inscricao_estadual"), rs.getString("razao_social"));
        }
        fornecedor.setId(rs.getInt("id"));
        return fornecedor;
    }
}