package com.loja.repository;

import com.loja.model.Produto;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import com.loja.repositiry.ConnectionFactory;
import com.loja.model.Produto;
import java.sql.*;

public class ProdutoDAO {

    public void salvar(Produto produto) {
        String sql = "MERGE INTO produto KEY(id) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produto.getId());
            stmt.setString(2, produto.getNome());
            stmt.setDouble(3, produto.getPreco());
            stmt.setInt(4, produto.getEstoque());

            stmt.executeUpdate();
            System.out.println("[DB] Produto salvo/atualizado: " + produto.getNome());

        } catch (SQLException e) {
            System.err.println("[ERRO DB] " + e.getMessage());
        }
    }

    public List<Produto> buscarTodos() {
        List<Produto> produtos = new ArrayList<>();
        String sql = "SELECT * FROM produto";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                produtos.add(new Produto(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        rs.getDouble("preco"),
                        rs.getInt("quantidade")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] " + e.getMessage());
        }
        return produtos;
    }
}