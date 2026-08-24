package com.loja.repository;

import com.loja.model.PecaItem;
import com.loja.model.ServicoItem;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CatalogoDAO {

    public List<ServicoItem> listarServicos() {
        List<ServicoItem> lista = new ArrayList<>();
        String sql = "SELECT id, descricao, valor_padrao FROM catalogo_servicos ORDER BY descricao ASC";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new ServicoItem(
                        rs.getInt("id"),
                        0,
                        rs.getString("descricao"),
                        rs.getDouble("valor_padrao")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao listar catálogo de serviços: " + e.getMessage());
        }
        return lista;
    }

    public void salvarServicoSeNaoExistir(String descricao, double valorPadrao) {
        if (descricao == null || descricao.trim().isEmpty()) return;
        String sql = "INSERT INTO catalogo_servicos (descricao, valor_padrao) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE valor_padrao = VALUES(valor_padrao)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, descricao.trim());
            stmt.setDouble(2, valorPadrao);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao salvar serviço no catálogo: " + e.getMessage());
        }
    }

    public List<PecaItem> listarPecas() {
        List<PecaItem> lista = new ArrayList<>();
        String sql = "SELECT id, nome, valor_padrao FROM catalogo_pecas ORDER BY nome ASC";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(new PecaItem(
                        rs.getInt("id"),
                        rs.getString("nome"),
                        1,
                        rs.getDouble("valor_padrao")
                ));
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao listar catálogo de peças: " + e.getMessage());
        }
        return lista;
    }

    public void salvarPecaSeNaoExistir(String nome, double valorPadrao) {
        if (nome == null || nome.trim().isEmpty()) return;
        String sql = "INSERT INTO catalogo_pecas (nome, valor_padrao) VALUES (?, ?) " +
                     "ON DUPLICATE KEY UPDATE valor_padrao = VALUES(valor_padrao)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nome.trim());
            stmt.setDouble(2, valorPadrao);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao salvar peça no catálogo: " + e.getMessage());
        }
    }
}
