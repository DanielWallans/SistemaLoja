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
        String sql = "INSERT INTO produto (id, codigo_barras, nome, preco, estoque) VALUES (?, ?, ?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE codigo_barras = ?, nome = ?, preco = ?, estoque = ?";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, produto.getId());
            stmt.setString(2, produto.getCodigoBarras());
            stmt.setString(3, produto.getNome());
            stmt.setDouble(4, produto.getPreco());
            stmt.setInt(5, produto.getEstoque());
            
            stmt.setString(6, produto.getCodigoBarras());
            stmt.setString(7, produto.getNome());
            stmt.setDouble(8, produto.getPreco());
            stmt.setInt(9, produto.getEstoque());

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
        String sql = "SELECT id, codigo_barras, nome, preco, estoque FROM produto ORDER BY nome ASC";

        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                produtos.add(new Produto(
                        rs.getInt("id"),
                        rs.getString("codigo_barras"),
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

    public Produto buscarPorId(int id) {
        String sql = "SELECT id, codigo_barras, nome, preco, estoque FROM produto WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Produto(
                            rs.getInt("id"),
                            rs.getString("codigo_barras"),
                            rs.getString("nome"),
                            rs.getDouble("preco"),
                            rs.getInt("estoque")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao buscar produto por ID: " + e.getMessage());
        }
        return null;
    }

    public Produto buscarPorCodigoOuId(String termo) {
        if (termo == null || termo.trim().isEmpty()) return null;
        termo = termo.trim();

        // 1. Busca por código de barras exato
        String sqlCod = "SELECT id, codigo_barras, nome, preco, estoque FROM produto WHERE codigo_barras = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlCod)) {
            stmt.setString(1, termo);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Produto(
                            rs.getInt("id"),
                            rs.getString("codigo_barras"),
                            rs.getString("nome"),
                            rs.getDouble("preco"),
                            rs.getInt("estoque")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao buscar produto por código de barras: " + e.getMessage());
        }

        // 2. Busca por ID numérico se for número
        try {
            int id = Integer.parseInt(termo);
            Produto p = buscarPorId(id);
            if (p != null) return p;
        } catch (NumberFormatException ignored) {}

        // 3. Busca aproximada por nome
        String sqlNome = "SELECT id, codigo_barras, nome, preco, estoque FROM produto WHERE LOWER(nome) LIKE ? LIMIT 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sqlNome)) {
            stmt.setString(1, "%" + termo.toLowerCase() + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Produto(
                            rs.getInt("id"),
                            rs.getString("codigo_barras"),
                            rs.getString("nome"),
                            rs.getDouble("preco"),
                            rs.getInt("estoque")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao buscar produto por nome aproximado: " + e.getMessage());
        }

        return null;
    }

    public List<Produto> buscarPorTermo(String termo) {
        List<Produto> lista = new ArrayList<>();
        if (termo == null) termo = "";
        String sql = "SELECT id, codigo_barras, nome, preco, estoque FROM produto " +
                     "WHERE LOWER(nome) LIKE ? OR codigo_barras LIKE ? ORDER BY nome ASC LIMIT 50";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, "%" + termo.toLowerCase() + "%");
            stmt.setString(2, "%" + termo + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Produto(
                            rs.getInt("id"),
                            rs.getString("codigo_barras"),
                            rs.getString("nome"),
                            rs.getDouble("preco"),
                            rs.getInt("estoque")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao buscar produtos por termo: " + e.getMessage());
        }
        return lista;
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