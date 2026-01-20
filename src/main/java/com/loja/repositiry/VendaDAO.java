package com.loja.repositiry;

import com.loja.model.Venda;
import com.loja.repositiry.ConnectionFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class VendaDAO {
    public void registrarVenda(Venda venda) {
        String sql = "INSERT INTO venda (produto_id, quantidade, valor_total) VALUES (?, ?, ?)";

        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, venda.getProdutoId());
            stmt.setInt(2, venda.getQuantidade());
            stmt.setDouble(3, venda.getValorTotal());

            stmt.executeUpdate();
            System.out.println("[DB] Venda registrada no histórico com sucesso.");

        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao registrar histórico: " + e.getMessage());
        }
    }
}