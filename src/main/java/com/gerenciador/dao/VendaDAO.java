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
import java.sql.Connection;
import java.sql.SQLException;
public class VendaDAO {

    public void salvar(Venda venda) throws SQLException {
        String sql = "INSERT INTO vendas (id_cliente, tipo_item, id_item, quantidade, valor_unidade, valor_total, data_venda, data_limite, data_pagamento, pago, observacao, nome_pdf) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseSetup.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, venda.getCliente().getId());


            if (venda.getItem() instanceof Produto) {
                stmt.setString(2, "PRODUTO");
            } else if (venda.getItem() instanceof Servico) {
                stmt.setString(2, "SERVICO");
            } else if (venda.getItem() instanceof MateriaPrima) {
                stmt.setString(2, "MATERIA_PRIMA");
            }
            stmt.setInt(3, venda.getItem().getId());

            stmt.setDouble(4, venda.getQuantidade().doubleValue());
            stmt.setDouble(5, venda.getValorUnidade().doubleValue());
            stmt.setDouble(6, venda.getValorTotal().doubleValue());
            stmt.setString(7, venda.getDataTransacao().toString());
            stmt.setString(8, venda.getDataLimite().toString());

            if (venda.getDataPagamento() != null) stmt.setString(9, venda.getDataPagamento().toString());
            else stmt.setNull(9, Types.VARCHAR);

            stmt.setInt(10, venda.getEstaPago() ? 1 : 0);
            stmt.setString(11, venda.getObservacao());
            stmt.setString(12, venda.getNomeArquivoPdf());

            stmt.executeUpdate();

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) venda.setId(rs.getInt(1));
            }
        }
    }

    public void atualizar(Venda venda) throws SQLException {
        String sql = "UPDATE vendas SET id_cliente=?, tipo_item=?, id_item=?, quantidade=?, valor_unidade=?, valor_total=?, data_venda=?, data_limite=?, data_pagamento=?, pago=?, observacao=?, nome_pdf=? WHERE id=?";
        try (Connection conn = DatabaseSetup.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, venda.getCliente().getId());

            if (venda.getItem() instanceof Produto) stmt.setString(2, "PRODUTO");
            else if (venda.getItem() instanceof Servico) stmt.setString(2, "SERVICO");
            else stmt.setString(2, "MATERIA_PRIMA");

            stmt.setInt(3, venda.getItem().getId());
            stmt.setDouble(4, venda.getQuantidade().doubleValue());
            stmt.setDouble(5, venda.getValorUnidade().doubleValue());
            stmt.setDouble(6, venda.getValorTotal().doubleValue());
            stmt.setString(7, venda.getDataTransacao().toString());
            stmt.setString(8, venda.getDataLimite().toString());

            if (venda.getDataPagamento() != null) stmt.setString(9, venda.getDataPagamento().toString());
            else stmt.setNull(9, Types.VARCHAR);

            stmt.setInt(10, venda.getEstaPago() ? 1 : 0);
            stmt.setString(11, venda.getObservacao());
            stmt.setString(12, venda.getNomeArquivoPdf());
            stmt.setInt(13, venda.getId());

            stmt.executeUpdate();
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

                String tipoItem = rs.getString("tipo_item");
                int idItem = rs.getInt("id_item");

                com.gerenciador.model.comercializavel.Comercializavel item = null;
                if ("PRODUTO".equals(tipoItem)) {
                    item = produtoDAO.buscarPorId(idItem);
                } else if ("SERVICO".equals(tipoItem)) {
                    item = servicoDAO.buscarPorId(idItem);
                } else if ("MATERIA_PRIMA".equals(tipoItem)) {
                    item = materiaPrimaDAO.buscarPorId(idItem);
                }


                LocalDate dataVenda = LocalDate.parse(rs.getString("data_venda"));
                LocalDate dataLimite = LocalDate.parse(rs.getString("data_limite"));
                String dataPagamentoStr = rs.getString("data_pagamento");
                LocalDate dataPagamento = dataPagamentoStr != null ? LocalDate.parse(dataPagamentoStr) : null;

                Venda venda;
                if (item instanceof Produto produto) {
                    java.util.Map<MateriaPrima, BigDecimal> materiasUsadas = new java.util.HashMap<>();
                    for (MateriaPrima mp : produto.getMateriasPrimasNecessarias()) {
                        materiasUsadas.put(mp, BigDecimal.ONE);
                    }
                    com.gerenciador.model.transacao.ComposicaoVenda composicao = new com.gerenciador.model.transacao.ComposicaoVenda(materiasUsadas);

                    venda = new Venda(dataVenda, cliente, produto, BigDecimal.valueOf(rs.getDouble("quantidade")), BigDecimal.valueOf(rs.getDouble("valor_unidade")), composicao, rs.getInt("pago") == 1, dataLimite, dataPagamento, null, rs.getString("observacao"));
                } else {
                    venda = new Venda(dataVenda, cliente, item, BigDecimal.valueOf(rs.getDouble("quantidade")), BigDecimal.valueOf(rs.getDouble("valor_unidade")), rs.getInt("pago") == 1, dataLimite, dataPagamento, null, rs.getString("observacao"));
                }

                venda.setId(rs.getInt("id"));
                venda.setValorTotal(BigDecimal.valueOf(rs.getDouble("valor_total")));
                venda.setNomeArquivoPdf(rs.getString("nome_pdf"));
                lista.add(venda);
            }
        }
        return lista;
    }
}