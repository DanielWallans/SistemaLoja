package com.loja.repository;

import com.loja.model.OSFoto;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class OSFotoDAO {

    public boolean salvar(OSFoto foto) {
        String sql = "INSERT INTO os_fotos (os_id, nome_arquivo, descricao, dados, miniatura, tamanho_bytes, operador, data_upload) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, foto.getOsId());
            stmt.setString(2, foto.getNomeArquivo() != null ? foto.getNomeArquivo() : "foto.jpg");
            stmt.setString(3, foto.getDescricao() != null ? foto.getDescricao() : "");
            stmt.setBytes(4, foto.getDados());
            stmt.setBytes(5, foto.getMiniatura());
            stmt.setLong(6, foto.getTamanhoBytes());
            stmt.setString(7, foto.getOperador() != null ? foto.getOperador() : "Técnico");
            stmt.setTimestamp(8, Timestamp.valueOf(foto.getDataUpload() != null ? foto.getDataUpload() : LocalDateTime.now()));

            int affected = stmt.executeUpdate();
            if (affected > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        foto.setId(rs.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao salvar foto da OS #" + foto.getOsId() + ": " + e.getMessage());
        }
        return false;
    }

    /**
     * Lista metadados e miniaturas de todas as fotos de uma OS (sem carregar o blob pesado completo).
     */
    public List<OSFoto> listarMiniaturasPorOS(int osId) {
        List<OSFoto> lista = new ArrayList<>();
        String sql = "SELECT id, os_id, nome_arquivo, descricao, miniatura, tamanho_bytes, operador, data_upload " +
                     "FROM os_fotos WHERE os_id = ? ORDER BY id ASC";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, osId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    OSFoto f = new OSFoto();
                    f.setId(rs.getInt("id"));
                    f.setOsId(rs.getInt("os_id"));
                    f.setNomeArquivo(rs.getString("nome_arquivo"));
                    f.setDescricao(rs.getString("descricao"));
                    f.setMiniatura(rs.getBytes("miniatura"));
                    f.setTamanhoBytes(rs.getLong("tamanho_bytes"));
                    f.setOperador(rs.getString("operador"));

                    Timestamp ts = rs.getTimestamp("data_upload");
                    if (ts != null) {
                        f.setDataUpload(ts.toLocalDateTime());
                    }
                    lista.add(f);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao listar miniaturas da OS #" + osId + ": " + e.getMessage());
        }
        return lista;
    }

    /**
     * Obtém os bytes da imagem completa em alta resolução.
     */
    public byte[] obterDadosCompletos(int fotoId) {
        String sql = "SELECT dados FROM os_fotos WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fotoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBytes("dados");
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao obter dados da foto #" + fotoId + ": " + e.getMessage());
        }
        return null;
    }

    public OSFoto buscarPorId(int fotoId) {
        String sql = "SELECT id, os_id, nome_arquivo, descricao, dados, miniatura, tamanho_bytes, operador, data_upload " +
                     "FROM os_fotos WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fotoId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    OSFoto f = new OSFoto();
                    f.setId(rs.getInt("id"));
                    f.setOsId(rs.getInt("os_id"));
                    f.setNomeArquivo(rs.getString("nome_arquivo"));
                    f.setDescricao(rs.getString("descricao"));
                    f.setDados(rs.getBytes("dados"));
                    f.setMiniatura(rs.getBytes("miniatura"));
                    f.setTamanhoBytes(rs.getLong("tamanho_bytes"));
                    f.setOperador(rs.getString("operador"));

                    Timestamp ts = rs.getTimestamp("data_upload");
                    if (ts != null) {
                        f.setDataUpload(ts.toLocalDateTime());
                    }
                    return f;
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao buscar foto #" + fotoId + ": " + e.getMessage());
        }
        return null;
    }

    public boolean excluir(int fotoId) {
        String sql = "DELETE FROM os_fotos WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, fotoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao excluir foto #" + fotoId + ": " + e.getMessage());
        }
        return false;
    }

    public boolean atualizarDescricao(int fotoId, String novaDescricao) {
        String sql = "UPDATE os_fotos SET descricao = ? WHERE id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, novaDescricao != null ? novaDescricao.trim() : "");
            stmt.setInt(2, fotoId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao atualizar legenda da foto #" + fotoId + ": " + e.getMessage());
        }
        return false;
    }

    public int contarFotosPorOS(int osId) {
        String sql = "SELECT COUNT(*) FROM os_fotos WHERE os_id = ?";
        try (Connection conn = ConnectionFactory.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, osId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            System.err.println("[ERRO DB] Falha ao contar fotos da OS #" + osId + ": " + e.getMessage());
        }
        return 0;
    }
}
