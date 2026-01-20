package com.loja.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {
    private static final String URL = "jdbc:h2:./banco_loja";
    private static final String USER = "sa";
    private static final String PASS = "";

    static {
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[ERRO] Driver do H2 não encontrado no projeto!");
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public static void criarTabela() {
        String sqlProduto = "CREATE TABLE IF NOT EXISTS produto (" +
                "id INT PRIMARY KEY, " +
                "nome VARCHAR(100), " +
                "preco DOUBLE, " +
                "quantidade INT)";


        String sqlVendas = "CREATE TABLE IF NOT EXISTS venda (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "produto_id INT, " +
                "quantidade INT, " +
                "valor_total DOUBLE, " +
                "data_hora TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (produto_id) REFERENCES produto(id))";

        try (Connection conn = getConnection();
             java.sql.Statement stmt = conn.createStatement()) {


            stmt.execute(sqlProduto);


            stmt.execute(sqlVendas);

            System.out.println("[DB] Tabelas de produtos e vendas verificadas/criadas.");

        } catch (SQLException e) {
            System.err.println("[ERRO] Falha ao organizar o banco: " + e.getMessage());
        }
    }
}
