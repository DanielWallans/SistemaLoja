package com.loja.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.loja.model.Produto;

public class ProdutoDAO {

    public boolean salvar(Produto produto) {
        String sql = "INSERT INTO produto (id, nome, preco, estoque) VALUES (?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE nome = ?, preco = ?, estoque = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produto.getId());
            stmt.setString(2, produto.getNome());
            stmt.setDouble(3, produto.getPreco());
            stmt.setInt(4, produto.getEstoque());
            
            stmt.setString(5, produto.getNome());
            stmt.setDouble(6, produto.getPreco());
            stmt.setInt(7, produto.getEstoque());

            stmt.executeUpdate();
            System.out.println("[DB] Produto salvo/atualizado: " + produto.getNome());
            return true;

        } catch (SQLException e) {
            System.err.println("[ERRO DB] " + e.getMessage());
            return false;
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
                        rs.getInt("estoque")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] " + e.getMessage());
        }
        return produtos;
    }

    public int obterProximoId() {
        String sql = "SELECT COALESCE(MAX(id), 0) + 1 AS proximo_id FROM produto";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt("proximo_id");
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao obter próximo ID: " + e.getMessage());
        }
        return 1;
    }
}