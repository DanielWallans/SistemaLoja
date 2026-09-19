package com.loja.repository;

import com.loja.model.AutoBackupConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

public class BackupConfigDAO {

    private static final String PROP_ATIVO = "backup_auto_ativo";
    private static final String PROP_POS_OPERACAO = "backup_salvar_pos_operacao";
    private static final String PROP_AO_FECHAR = "backup_salvar_ao_fechar";
    private static final String PROP_PERIODICO = "backup_salvar_periodico";
    private static final String PROP_INTERVALO = "backup_intervalo_minutos";
    private static final String PROP_DESTINO = "backup_pasta_destino";
    private static final String PROP_SECUNDARIA = "backup_pasta_secundaria";
    private static final String PROP_RETENCAO = "backup_retencao_max";
    private static final String PROP_LEMBRETE_INICIAL = "backup_lembrete_inicial";
    private static final String PROP_ULTIMO_TIME = "backup_ultimo_timestamp";
    private static final String PROP_ULTIMO_STATUS = "backup_ultimo_status";

    public AutoBackupConfig carregarConfiguracoes() {
        AutoBackupConfig config = new AutoBackupConfig();

        // 1. Tenta carregar do arquivo local espelho (caso banco esteja offline ou para inicialização rápida)
        carregarDoArquivoLocal(config);

        // 2. Se o banco estiver disponível, carrega do banco (sobrepondo com o valor mais oficial)
        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                String sql = "SELECT chave, valor FROM config_geral WHERE chave LIKE 'backup_%'";
                try (PreparedStatement stmt = conn.prepareStatement(sql);
                     ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String chave = rs.getString("chave");
                        String valor = rs.getString("valor");
                        if (valor == null) continue;

                        switch (chave) {
                            case PROP_ATIVO:
                                config.setAtivo(Boolean.parseBoolean(valor));
                                break;
                            case PROP_POS_OPERACAO:
                                config.setSalvarPosOperacaoCritica(Boolean.parseBoolean(valor));
                                break;
                            case PROP_AO_FECHAR:
                                config.setSalvarAoFechar(Boolean.parseBoolean(valor));
                                break;
                            case PROP_PERIODICO:
                                config.setSalvarPeriodico(Boolean.parseBoolean(valor));
                                break;
                            case PROP_INTERVALO:
                                try {
                                    config.setIntervaloMinutos(Integer.parseInt(valor));
                                } catch (NumberFormatException ignored) {}
                                break;
                            case PROP_DESTINO:
                                if (!valor.trim().isEmpty()) {
                                    config.setPastaDestino(valor.trim());
                                }
                                break;
                            case PROP_SECUNDARIA:
                                config.setPastaSecundaria(valor.trim());
                                break;
                            case PROP_RETENCAO:
                                try {
                                    config.setRetencaoMaxArquivos(Integer.parseInt(valor));
                                } catch (NumberFormatException ignored) {}
                                break;
                            case PROP_LEMBRETE_INICIAL:
                                config.setLembreteInicial(Boolean.parseBoolean(valor));
                                break;
                            case PROP_ULTIMO_TIME:
                                config.setUltimoBackupTimestamp(valor);
                                break;
                            case PROP_ULTIMO_STATUS:
                                config.setUltimoBackupStatus(valor);
                                break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("[AVISO] Não foi possível carregar configurações de backup do banco: " + e.getMessage());
        }

        return config;
    }

    public boolean salvarConfiguracoes(AutoBackupConfig config) {
        if (config == null) return false;

        // 1. Salva no arquivo local para garantir redundância
        salvarNoArquivoLocal(config);

        // 2. Salva na tabela config_geral do MySQL
        String sql = "INSERT INTO config_geral (chave, valor) VALUES (?, ?) ON DUPLICATE KEY UPDATE valor = ?";
        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                conn.setAutoCommit(false);
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    salvarParametro(stmt, PROP_ATIVO, String.valueOf(config.isAtivo()));
                    salvarParametro(stmt, PROP_POS_OPERACAO, String.valueOf(config.isSalvarPosOperacaoCritica()));
                    salvarParametro(stmt, PROP_AO_FECHAR, String.valueOf(config.isSalvarAoFechar()));
                    salvarParametro(stmt, PROP_PERIODICO, String.valueOf(config.isSalvarPeriodico()));
                    salvarParametro(stmt, PROP_INTERVALO, String.valueOf(config.getIntervaloMinutos()));
                    salvarParametro(stmt, PROP_DESTINO, config.getPastaDestino());
                    salvarParametro(stmt, PROP_SECUNDARIA, config.getPastaSecundaria());
                    salvarParametro(stmt, PROP_RETENCAO, String.valueOf(config.getRetencaoMaxArquivos()));
                    salvarParametro(stmt, PROP_LEMBRETE_INICIAL, String.valueOf(config.isLembreteInicial()));
                    salvarParametro(stmt, PROP_ULTIMO_TIME, config.getUltimoBackupTimestamp());
                    salvarParametro(stmt, PROP_ULTIMO_STATUS, config.getUltimoBackupStatus());
                    conn.commit();
                    return true;
                } catch (SQLException e) {
                    conn.rollback();
                    System.err.println("[ERRO DB] Falha ao salvar configurações de backup no banco: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            System.err.println("[AVISO DB] Banco indisponível ao salvar backup config: " + e.getMessage());
        }

        return true; // Retorna true pois foi persistido no arquivo local
    }

    public void atualizarStatusUltimoBackup(String timestamp, String status) {
        String sql = "INSERT INTO config_geral (chave, valor) VALUES (?, ?) ON DUPLICATE KEY UPDATE valor = ?";
        try (Connection conn = ConnectionFactory.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    salvarParametro(stmt, PROP_ULTIMO_TIME, timestamp);
                    salvarParametro(stmt, PROP_ULTIMO_STATUS, status);
                }
            }
        } catch (Exception ignored) {}

        // Atualiza também arquivo local
        File local = getArquivoLocalConfig();
        if (local != null && local.exists()) {
            try {
                Properties props = new Properties();
                try (FileInputStream fis = new FileInputStream(local)) {
                    props.load(fis);
                }
                props.setProperty(PROP_ULTIMO_TIME, timestamp != null ? timestamp : "");
                props.setProperty(PROP_ULTIMO_STATUS, status != null ? status : "");
                try (FileOutputStream fos = new FileOutputStream(local)) {
                    props.store(fos, "Configurações de Backup do SystemPro");
                }
            } catch (Exception ignored) {}
        }
    }

    private void salvarParametro(PreparedStatement stmt, String chave, String valor) throws SQLException {
        stmt.setString(1, chave);
        stmt.setString(2, valor != null ? valor : "");
        stmt.setString(3, valor != null ? valor : "");
        stmt.executeUpdate();
    }

    private File getArquivoLocalConfig() {
        try {
            String appData = System.getenv("APPDATA");
            File baseDir;
            if (appData != null && !appData.trim().isEmpty()) {
                baseDir = new File(appData, "SystemPro");
            } else {
                baseDir = new File(System.getProperty("user.home", "."), ".systempro");
            }
            if (!baseDir.exists()) {
                baseDir.mkdirs();
            }
            return new File(baseDir, "backup_config.properties");
        } catch (Exception e) {
            return new File("backup_config.properties");
        }
    }

    private void carregarDoArquivoLocal(AutoBackupConfig config) {
        File file = getArquivoLocalConfig();
        if (file != null && file.exists()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                if (props.containsKey(PROP_ATIVO)) config.setAtivo(Boolean.parseBoolean(props.getProperty(PROP_ATIVO)));
                if (props.containsKey(PROP_POS_OPERACAO)) config.setSalvarPosOperacaoCritica(Boolean.parseBoolean(props.getProperty(PROP_POS_OPERACAO)));
                if (props.containsKey(PROP_AO_FECHAR)) config.setSalvarAoFechar(Boolean.parseBoolean(props.getProperty(PROP_AO_FECHAR)));
                if (props.containsKey(PROP_PERIODICO)) config.setSalvarPeriodico(Boolean.parseBoolean(props.getProperty(PROP_PERIODICO)));
                if (props.containsKey(PROP_INTERVALO)) {
                    try { config.setIntervaloMinutos(Integer.parseInt(props.getProperty(PROP_INTERVALO))); } catch (Exception ignored) {}
                }
                if (props.containsKey(PROP_DESTINO) && !props.getProperty(PROP_DESTINO).trim().isEmpty()) {
                    config.setPastaDestino(props.getProperty(PROP_DESTINO).trim());
                }
                if (props.containsKey(PROP_SECUNDARIA)) {
                    config.setPastaSecundaria(props.getProperty(PROP_SECUNDARIA).trim());
                }
                if (props.containsKey(PROP_RETENCAO)) {
                    try { config.setRetencaoMaxArquivos(Integer.parseInt(props.getProperty(PROP_RETENCAO))); } catch (Exception ignored) {}
                }
                if (props.containsKey(PROP_LEMBRETE_INICIAL)) config.setLembreteInicial(Boolean.parseBoolean(props.getProperty(PROP_LEMBRETE_INICIAL)));
                if (props.containsKey(PROP_ULTIMO_TIME)) config.setUltimoBackupTimestamp(props.getProperty(PROP_ULTIMO_TIME));
                if (props.containsKey(PROP_ULTIMO_STATUS)) config.setUltimoBackupStatus(props.getProperty(PROP_ULTIMO_STATUS));
            } catch (IOException e) {
                System.err.println("[AVISO] Não foi possível ler backup_config.properties: " + e.getMessage());
            }
        }
    }

    private void salvarNoArquivoLocal(AutoBackupConfig config) {
        File file = getArquivoLocalConfig();
        if (file == null) return;
        try {
            if (file.getParentFile() != null && !file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            Properties props = new Properties();
            props.setProperty(PROP_ATIVO, String.valueOf(config.isAtivo()));
            props.setProperty(PROP_POS_OPERACAO, String.valueOf(config.isSalvarPosOperacaoCritica()));
            props.setProperty(PROP_AO_FECHAR, String.valueOf(config.isSalvarAoFechar()));
            props.setProperty(PROP_PERIODICO, String.valueOf(config.isSalvarPeriodico()));
            props.setProperty(PROP_INTERVALO, String.valueOf(config.getIntervaloMinutos()));
            props.setProperty(PROP_DESTINO, config.getPastaDestino() != null ? config.getPastaDestino() : "");
            props.setProperty(PROP_SECUNDARIA, config.getPastaSecundaria() != null ? config.getPastaSecundaria() : "");
            props.setProperty(PROP_RETENCAO, String.valueOf(config.getRetencaoMaxArquivos()));
            props.setProperty(PROP_LEMBRETE_INICIAL, String.valueOf(config.isLembreteInicial()));
            props.setProperty(PROP_ULTIMO_TIME, config.getUltimoBackupTimestamp() != null ? config.getUltimoBackupTimestamp() : "");
            props.setProperty(PROP_ULTIMO_STATUS, config.getUltimoBackupStatus() != null ? config.getUltimoBackupStatus() : "");

            try (FileOutputStream fos = new FileOutputStream(file)) {
                props.store(fos, "SystemPro - Configuracoes de Backup Automatico");
            }
        } catch (Exception e) {
            System.err.println("[AVISO] Não foi possível salvar backup_config.properties: " + e.getMessage());
        }
    }
}
