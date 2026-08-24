package com.loja.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import com.loja.model.Equipamento;

public class EquipamentoDAO {

    public boolean salvar(Equipamento equipamento) {
        if (equipamento.getId() > 0) {
            String sql = "UPDATE equipamento SET cliente_id = ?, tipo = ?, marca = ?, modelo = ?, numero_serie = ?, " +
                         "cor = ?, avarias = ?, patrimonio = ?, senha_acesso = ?, acessorios = ? WHERE id = ?";
            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, equipamento.getClienteId());
                stmt.setString(2, equipamento.getTipo());
                stmt.setString(3, equipamento.getMarca());
                stmt.setString(4, equipamento.getModelo());
                stmt.setString(5, equipamento.getNumeroSerie());
                stmt.setString(6, equipamento.getCor());
                stmt.setString(7, equipamento.getAvarias());
                stmt.setString(8, equipamento.getPatrimonio());
                stmt.setString(9, equipamento.getSenhaAcesso());
                stmt.setString(10, equipamento.getAcessorios());
                stmt.setInt(11, equipamento.getId());
                return stmt.executeUpdate() > 0;
            } catch (SQLException e) {
                System.err.println("[ERRO DB] Falha ao atualizar equipamento: " + e.getMessage());
                return false;
            }
        } else {
            String sql = "INSERT INTO equipamento (cliente_id, tipo, marca, modelo, numero_serie, cor, avarias, patrimonio, senha_acesso, acessorios) " +
                         "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
            try (Connection conn = ConnectionFactory.getConnection();
                 PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                stmt.setInt(1, equipamento.getClienteId());
                stmt.setString(2, equipamento.getTipo());
                stmt.setString(3, equipamento.getMarca());
                stmt.setString(4, equipamento.getModelo());
                stmt.setString(5, equipamento.getNumeroSerie());
                stmt.setString(6, equipamento.getCor());
                stmt.setString(7, equipamento.getAvarias());
                stmt.setString(8, equipamento.getPatrimonio());
                stmt.setString(9, equipamento.getSenhaAcesso());
                stmt.setString(10, equipamento.getAcessorios());
                int rows = stmt.executeUpdate();
                if (rows > 0) {
                    try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            equipamento.setId(generatedKeys.getInt(1));
                        }
                    }
                    return true;
                }
                return false;
            } catch (SQLException e) {
                System.err.println("[ERRO DB] Falha ao cadastrar equipamento: " + e.getMessage());
                return false;
            }
        }
    }

    public List<Equipamento> buscarPorCliente(int clienteId) {
        List<Equipamento> lista = new ArrayList<>();
        String sql = "SELECT * FROM equipamento WHERE cliente_id = ? ORDER BY id DESC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, clienteId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    lista.add(new Equipamento(
                            rs.getInt("id"),
                            rs.getInt("cliente_id"),
                            rs.getString("tipo"),
                            rs.getString("marca"),
                            rs.getString("modelo"),
                            rs.getString("numero_serie"),
                            rs.getString("cor"),
                            rs.getString("avarias"),
                            rs.getString("patrimonio"),
                            rs.getString("senha_acesso"),
                            rs.getString("acessorios")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao buscar equipamentos: " + e.getMessage());
        }
        return lista;
    }

    public Equipamento buscarPorId(int id) {
        String sql = "SELECT * FROM equipamento WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Equipamento(
                            rs.getInt("id"),
                            rs.getInt("cliente_id"),
                            rs.getString("tipo"),
                            rs.getString("marca"),
                            rs.getString("modelo"),
                            rs.getString("numero_serie"),
                            rs.getString("cor"),
                            rs.getString("avarias"),
                            rs.getString("patrimonio"),
                            rs.getString("senha_acesso"),
                            rs.getString("acessorios")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao buscar equipamento: " + e.getMessage());
        }
        return null;
    }
}
