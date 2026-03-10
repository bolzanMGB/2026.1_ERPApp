package com.gerenciador.dao;
import com.gerenciador.model.cliente.Cliente;
import com.gerenciador.model.cliente.ClientePF;
import com.gerenciador.model.cliente.ClientePJ;
import com.gerenciador.model.pessoa.Endereco;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.gerenciador.app.DatabaseSetup;
import java.sql.Connection;
import java.sql.SQLException;
public class ClienteDAO {
    public void atualizar(Cliente cliente) throws SQLException {
        String sql = "UPDATE clientes SET tipo_pessoa = ?, nome = ?, telefone = ?, cidade = ?, bairro = ?, rua = ?, numero_casa = ?, documento = ?, inscricao_estadual = ?, razao_social = ?, nome_responsavel = ? WHERE id = ?";

        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            configurarStatement(stmt, cliente);

            stmt.setInt(12, cliente.getId());

            stmt.executeUpdate();
        }
    }

    private void configurarStatement(PreparedStatement stmt, Cliente cliente) throws SQLException {
        boolean isPF = cliente instanceof ClientePF;

        stmt.setString(1, isPF ? "PF" : "PJ");
        stmt.setString(2, cliente.getNomePrincipal());
        stmt.setString(3, cliente.getTelefone());
        stmt.setString(4, cliente.getEndereco().getCidade());
        stmt.setString(5, cliente.getEndereco().getBairro());
        stmt.setString(6, cliente.getEndereco().getRua());
        stmt.setString(7, cliente.getEndereco().getnumeroCasa());
        stmt.setString(8, cliente.getDocumento());

        if (isPF) {
            stmt.setNull(9, Types.VARCHAR);
            stmt.setNull(10, Types.VARCHAR);
            stmt.setNull(11, Types.VARCHAR);
        } else {
            ClientePJ pj = (ClientePJ) cliente;
            stmt.setString(9, pj.getInscricaoEstadual());
            stmt.setString(10, pj.getRazaoSocial());
            stmt.setString(11, pj.getNomeResponsavel());
        }
    }

    public void salvar(Cliente cliente) throws SQLException {
        String sql = "INSERT INTO clientes (tipo_pessoa, nome, telefone, cidade, bairro, rua, numero_casa, documento, inscricao_estadual, razao_social, nome_responsavel) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            boolean isPF = cliente instanceof ClientePF;
            stmt.setString(1, isPF ? "PF" : "PJ");
            stmt.setString(2, cliente.getNomePrincipal());
            stmt.setString(3, cliente.getTelefone());
            stmt.setString(4, cliente.getEndereco().getCidade());
            stmt.setString(5, cliente.getEndereco().getBairro());
            stmt.setString(6, cliente.getEndereco().getRua());
            stmt.setString(7, cliente.getEndereco().getnumeroCasa());
            stmt.setString(8, cliente.getDocumento());

            if (isPF) {
                stmt.setNull(9, Types.VARCHAR);
                stmt.setNull(10, Types.VARCHAR);
                stmt.setNull(11, Types.VARCHAR);
            } else {
                ClientePJ pj = (ClientePJ) cliente;
                stmt.setString(9, pj.getInscricaoEstadual());
                stmt.setString(10, pj.getRazaoSocial());
                stmt.setString(11, pj.getNomeResponsavel());
            }

            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) cliente.setId(rs.getInt(1));
            }
        }
    }

    public List<Cliente> listarTodos() throws SQLException {
        List<Cliente> lista = new ArrayList<>();
        String sql = "SELECT * FROM clientes";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                Endereco end = new Endereco(rs.getString("cidade"), rs.getString("bairro"), rs.getString("rua"), rs.getString("numero_casa"));
                Cliente cliente;
                if ("PF".equals(rs.getString("tipo_pessoa"))) {
                    cliente = new ClientePF(rs.getString("nome"), rs.getString("documento"), rs.getString("telefone"), end);
                } else {
                    cliente = new ClientePJ(rs.getString("nome"), rs.getString("telefone"), end, rs.getString("documento"), rs.getString("nome_responsavel"), rs.getString("inscricao_estadual"), rs.getString("razao_social"));
                }
                cliente.setId(rs.getInt("id"));
                lista.add(cliente);
            }
        }
        return lista;
    }

    public void deletar(int id) throws SQLException {
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement("DELETE FROM clientes WHERE id = ?")) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
        }
    }

    public Cliente buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM clientes WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Endereco end = new Endereco(rs.getString("cidade"), rs.getString("bairro"), rs.getString("rua"), rs.getString("numero_casa"));
                    Cliente cliente;
                    if ("PF".equals(rs.getString("tipo_pessoa"))) {
                        cliente = new ClientePF(rs.getString("nome"), rs.getString("documento"), rs.getString("telefone"), end);
                    } else {
                        cliente = new ClientePJ(rs.getString("nome"), rs.getString("telefone"), end, rs.getString("documento"), rs.getString("nome_responsavel"), rs.getString("inscricao_estadual"), rs.getString("razao_social"));
                    }
                    cliente.setId(rs.getInt("id"));
                    return cliente;
                }
            }
        }
        return null;
    }


}