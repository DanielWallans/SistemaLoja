package com.loja.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CaixaDAO {

    public double obterSaldo() {
        String sql = "SELECT saldo FROM caixa WHERE id = 1";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("saldo");
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao obter saldo do caixa: " + e.getMessage());
        }
        return 100.00; // Valor padrão inicial
    }

    public void atualizarSaldo(double novoSaldo) {
        String sql = "UPDATE caixa SET saldo = ? WHERE id = 1";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, novoSaldo);
            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao atualizar saldo do caixa: " + e.getMessage());
        }
    }

    public void registrarSangria(double valor) {
        String sql = "INSERT INTO sangria (valor) VALUES (?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setDouble(1, valor);
            stmt.executeUpdate();
            System.out.println("[DB] Sangria de R$ " + valor + " registrada no banco.");
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao registrar sangria: " + e.getMessage());
        }
    }

    public List<String> obterHistoricoSangrias() {
        List<String> sangrias = new ArrayList<>();
        String sql = "SELECT valor, data_hora FROM sangria ORDER BY data_hora ASC";
        try (Connection conn = ConnectionFactory.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                double valor = rs.getDouble("valor");
                java.sql.Timestamp ts = rs.getTimestamp("data_hora");
                sangrias.add(String.format("Retirada: R$ %.2f em %s", valor, ts.toString()));
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao obter histórico de sangrias: " + e.getMessage());
        }
        return sangrias;
    }
}
