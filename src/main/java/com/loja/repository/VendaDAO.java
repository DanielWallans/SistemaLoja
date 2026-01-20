package com.loja.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.loja.model.Venda;

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
    public void listarVendas() {
        // O JOIN serve para pegar o nome do produto na outra tabela, nível profissional!
        String sql = "SELECT v.id, p.nome, v.quantidade, v.valor_total FROM venda v " +
                     "JOIN produto p ON v.produto_id = p.id";
        
        try (Connection conn = ConnectionFactory.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                System.out.println("ID: " + rs.getInt("id") + 
                                   " | Produto: " + rs.getString("nome") + 
                                   " | Qtd: " + rs.getInt("quantidade") + 
                                   " | Total: R$ " + rs.getDouble("valor_total"));
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao listar vendas: " + e.getMessage());
        }
    }
    
    public double calcularFaturamentoTotal() {
    // A função SUM(valor_total) soma todos os registros da coluna de uma vez
    String sql = "SELECT SUM(valor_total) AS total FROM venda";
    try (java.sql.Connection conn = com.loja.repository.ConnectionFactory.getConnection();
         java.sql.PreparedStatement stmt = conn.prepareStatement(sql);
         java.sql.ResultSet rs = stmt.executeQuery()) {
        
        if (rs.next()) {
            return rs.getDouble("total");
        }
    } catch (java.sql.SQLException e) {
        System.err.println("[ERRO DB] Falha ao calcular faturamento: " + e.getMessage());
    }
    return 0.0;
}
}