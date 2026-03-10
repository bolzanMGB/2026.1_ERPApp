package com.gerenciador.dao;

import com.gerenciador.model.comercializavel.MateriaPrima;
import com.gerenciador.model.comercializavel.Produto;

import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.gerenciador.app.DatabaseSetup;
import java.sql.Connection;
import java.sql.SQLException;
public class ProdutoDAO {

    public void salvar(Produto produto) throws SQLException {
        Connection conn = DatabaseSetup.getConnection();
         conn.setAutoCommit(false);

        try {

            String sqlProd = "INSERT INTO produtos (nome, total_vendido) VALUES (?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sqlProd, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setString(1, produto.getNome());
                stmt.setDouble(2, produto.getTotalVendido().doubleValue());
                stmt.executeUpdate();
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) produto.setId(rs.getInt(1));
                }
            }


            String sqlComp = "INSERT INTO produto_composicao (id_produto, id_materia_prima, quantidade_necessaria) VALUES (?, ?, ?)";
            try (PreparedStatement stmtComp = conn.prepareStatement(sqlComp)) {
                for (MateriaPrima mp : produto.getMateriasPrimasNecessarias()) {
                    stmtComp.setInt(1, produto.getId());
                    stmtComp.setInt(2, mp.getId());
                    stmtComp.setDouble(3, 1.0);
                    stmtComp.addBatch();
                }
                stmtComp.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public void deletar(int idProduto) throws SQLException {
        Connection conn = DatabaseSetup.getConnection();
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM produto_composicao WHERE id_produto = ?")) {
                stmt.setInt(1, idProduto);
                stmt.executeUpdate();
            }
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM produtos WHERE id = ?")) {
                stmt.setInt(1, idProduto);
                stmt.executeUpdate();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    public List<Produto> listarTodos() throws SQLException {
        List<Produto> lista = new ArrayList<>();
        String sqlProd = "SELECT * FROM produtos";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmtProd = conn.prepareStatement(sqlProd); ResultSet rsProd = stmtProd.executeQuery()) {
            while (rsProd.next()) {
                lista.add(montarProduto(conn, rsProd));
            }
        }
        return lista;
    }

    public Produto buscarPorId(int id) throws SQLException {
        String sql = "SELECT * FROM produtos WHERE id = ?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return montarProduto(conn, rs);
            }
        }
        return null;
    }

    private Produto montarProduto(Connection conn, ResultSet rsProd) throws SQLException {
        int idProduto = rsProd.getInt("id");
        String nome = rsProd.getString("nome");
        BigDecimal totalVendido = BigDecimal.valueOf(rsProd.getDouble("total_vendido"));

        List<MateriaPrima> materiasPrimas = new ArrayList<>();
        String sqlComp = "SELECT mp.* FROM materias_primas mp INNER JOIN produto_composicao pc ON mp.id = pc.id_materia_prima WHERE pc.id_produto = ?";
        try (PreparedStatement stmtComp = conn.prepareStatement(sqlComp)) {
            stmtComp.setInt(1, idProduto);
            try (ResultSet rsComp = stmtComp.executeQuery()) {
                while (rsComp.next()) {
                    MateriaPrima mp = new MateriaPrima(rsComp.getString("nome"), rsComp.getString("unidade"));
                    mp.setId(rsComp.getInt("id"));
                    mp.setEstoqueAtual(BigDecimal.valueOf(rsComp.getDouble("estoque_atual")));
                    materiasPrimas.add(mp);
                }
            }
        }

        Produto produto = new Produto(nome, materiasPrimas);
        produto.setId(idProduto);
        produto.setTotalVendido(totalVendido);
        return produto;
    }

    public void atualizar(Produto produto) throws SQLException {
        Connection conn = DatabaseSetup.getConnection();
        conn.setAutoCommit(false);
        try {
            try (PreparedStatement stmt = conn.prepareStatement("UPDATE produtos SET nome = ?, total_vendido = ? WHERE id = ?")) {
                stmt.setString(1, produto.getNome());
                stmt.setDouble(2, produto.getTotalVendido().doubleValue());
                stmt.setInt(3, produto.getId());
                stmt.executeUpdate();
            }

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM produto_composicao WHERE id_produto = ?")) {
                stmt.setInt(1, produto.getId());
                stmt.executeUpdate();
            }
            try (PreparedStatement stmtComp = conn.prepareStatement("INSERT INTO produto_composicao (id_produto, id_materia_prima, quantidade_necessaria) VALUES (?, ?, ?)")) {
                for (MateriaPrima mp : produto.getMateriasPrimasNecessarias()) {
                    stmtComp.setInt(1, produto.getId());
                    stmtComp.setInt(2, mp.getId());
                    stmtComp.setDouble(3, 1.0);
                    stmtComp.addBatch();
                }
                stmtComp.executeBatch();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }
}