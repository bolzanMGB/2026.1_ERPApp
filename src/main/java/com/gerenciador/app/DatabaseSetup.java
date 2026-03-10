package com.gerenciador.app;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseSetup {

    private static final String RAIZ_APP = System.getProperty("user.home") + File.separator + "GerenciadorApp";
    private static final String DB_PATH = RAIZ_APP + File.separator + "sistema.db";
    private static final String URL = "jdbc:sqlite:" + DB_PATH;

    public static void main(String[] args) {
        inicializarBanco();
    }

    public static void inicializarBanco() {
        try {

            criarEstruturaDeDiretorios();

            try (Connection conn = DriverManager.getConnection(URL);
                 Statement stmt = conn.createStatement()) {


                stmt.execute("PRAGMA foreign_keys = ON;");


                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS clientes (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tipo_pessoa TEXT NOT NULL,
                        nome TEXT NOT NULL,
                        telefone TEXT,
                        cidade TEXT, bairro TEXT, rua TEXT, numero_casa TEXT,
                        documento TEXT UNIQUE,
                        inscricao_estadual TEXT, razao_social TEXT, nome_responsavel TEXT
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS fornecedores (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        tipo_pessoa TEXT NOT NULL,
                        nome TEXT NOT NULL,
                        telefone TEXT,
                        cidade TEXT, bairro TEXT, rua TEXT, numero_casa TEXT,
                        documento TEXT UNIQUE,
                        inscricao_estadual TEXT, razao_social TEXT, nome_responsavel TEXT
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS materias_primas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        unidade TEXT,
                        estoque_atual REAL DEFAULT 0,
                        total_comprado REAL DEFAULT 0,
                        total_vendido REAL DEFAULT 0
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS produtos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        total_vendido REAL DEFAULT 0
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS produto_composicao (
                        id_produto INTEGER,
                        id_materia_prima INTEGER,
                        quantidade_necessaria REAL,
                        PRIMARY KEY (id_produto, id_materia_prima),
                        FOREIGN KEY(id_produto) REFERENCES produtos(id) ON DELETE CASCADE,
                        FOREIGN KEY(id_materia_prima) REFERENCES materias_primas(id) ON DELETE CASCADE
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS servicos (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        nome TEXT NOT NULL,
                        total_vendido REAL DEFAULT 0
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS compras (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_fornecedor INTEGER,
                        id_materia_prima INTEGER,
                        quantidade REAL,
                        valor_unidade REAL,
                        valor_total REAL,
                        data_compra TEXT,
                        data_limite TEXT,
                        data_pagamento TEXT,
                        pago INTEGER,
                        observacao TEXT,
                        nome_pdf TEXT,
                        FOREIGN KEY(id_fornecedor) REFERENCES fornecedores(id) ON DELETE CASCADE,
                        FOREIGN KEY(id_materia_prima) REFERENCES materias_primas(id) ON DELETE CASCADE
                    );
                """);

                stmt.execute("""
                    CREATE TABLE IF NOT EXISTS vendas (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        id_cliente INTEGER,
                        tipo_item TEXT,
                        id_item INTEGER,
                        quantidade REAL,
                        valor_unidade REAL,
                        valor_total REAL,
                        data_venda TEXT,
                        data_limite TEXT,
                        data_pagamento TEXT,
                        pago INTEGER,
                        observacao TEXT,
                        nome_pdf TEXT,
                        FOREIGN KEY(id_cliente) REFERENCES clientes(id) ON DELETE CASCADE
                    );
                """);

                System.out.println("Banco de dados configurado em: " + DB_PATH);

            }
        } catch (Exception e) {
            System.err.println("Erro Crítico ao configurar banco: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void criarEstruturaDeDiretorios() {

        String[] pastasParaCriar = {
                RAIZ_APP,
                RAIZ_APP + File.separator + "PDF",
                RAIZ_APP + File.separator + "PDF" + File.separator + "Vendas",
                RAIZ_APP + File.separator + "PDF" + File.separator + "Compras"
        };

        for (String caminho : pastasParaCriar) {
            File pasta = new File(caminho);
            if (!pasta.exists()) {
                if (pasta.mkdirs()) {
                    System.out.println("Pasta criada: " + caminho);
                }
            }
        }
    }

  public static Connection getConnection() throws SQLException {
    return DriverManager.getConnection(URL);
  }

}