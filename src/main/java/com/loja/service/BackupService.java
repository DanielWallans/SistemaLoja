package com.loja.service;

import com.loja.repository.ConnectionFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupService {
    private static final DateTimeFormatter FORMATTER_ARQUIVO = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");

    private static final String[] TABELAS_ORDENADAS = {
            "usuario",
            "config_geral",
            "config_taxas",
            "caixa",
            "catalogo_pecas",
            "catalogo_servicos",
            "produto",
            "cliente",
            "equipamento",
            "ordem_servico",
            "os_pecas",
            "os_servicos",
            "os_historico",
            "caixa_sessao",
            "venda_pdv",
            "venda_pdv_itens",
            "venda_pagamento",
            "caixa_movimento",
            "sangria"
    };

    public static File gerarNomeArquivoPadrao(File diretorioDestino) {
        if (diretorioDestino == null || !diretorioDestino.exists()) {
            diretorioDestino = new File(System.getProperty("user.home"), "Documents");
            if (!diretorioDestino.exists()) {
                diretorioDestino = new File(".");
            }
        }
        String nome = "Backup_SistemaLoja_" + LocalDateTime.now().format(FORMATTER_ARQUIVO) + ".zip";
        return new File(diretorioDestino, nome);
    }

    public static ResultadoBackup realizarBackupZip(File arquivoZipDestino) {
        long inicio = System.currentTimeMillis();
        int totalLinhas = 0;
        int totalTabelas = 0;

        try (Connection conn = ConnectionFactory.getConnection()) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
                writer.println("-- ==========================================================");
                writer.println("-- BACKUP COMPLETO DO BANCO DE DADOS - SISTEMA ASSISTÊNCIA PRO");
                writer.println("-- Data de Exportação: " + LocalDateTime.now().toString());
                writer.println("-- ==========================================================");
                writer.println("SET FOREIGN_KEY_CHECKS = 0;");
                writer.println();

                DatabaseMetaData meta = conn.getMetaData();

                for (String tabela : TABELAS_ORDENADAS) {
                    try (ResultSet rsMeta = meta.getTables(null, null, tabela, new String[]{"TABLE"})) {
                        if (rsMeta.next()) {
                            totalTabelas++;
                            writer.println("-- ----------------------------------------------------------");
                            writer.println("-- Dados da Tabela: `" + tabela + "`");
                            writer.println("-- ----------------------------------------------------------");

                            // Buscar registros
                            String sql = "SELECT * FROM " + tabela;
                            try (Statement stmt = conn.createStatement();
                                 ResultSet rs = stmt.executeQuery(sql)) {
                                ResultSetMetaData rsmd = rs.getMetaData();
                                int colCount = rsmd.getColumnCount();

                                while (rs.next()) {
                                    StringBuilder sbInsert = new StringBuilder();
                                    sbInsert.append("INSERT INTO `").append(tabela).append("` (");
                                    for (int i = 1; i <= colCount; i++) {
                                        sbInsert.append("`").append(rsmd.getColumnName(i)).append("`");
                                        if (i < colCount) sbInsert.append(", ");
                                    }
                                    sbInsert.append(") VALUES (");

                                    for (int i = 1; i <= colCount; i++) {
                                        Object obj = rs.getObject(i);
                                        if (obj == null) {
                                            sbInsert.append("NULL");
                                        } else if (obj instanceof Number || obj instanceof Boolean) {
                                            sbInsert.append(obj.toString());
                                        } else {
                                            String str = obj.toString().replace("'", "''").replace("\\", "\\\\");
                                            sbInsert.append("'").append(str).append("'");
                                        }
                                        if (i < colCount) sbInsert.append(", ");
                                    }
                                    sbInsert.append(");");
                                    writer.println(sbInsert.toString());
                                    totalLinhas++;
                                }
                            }
                            writer.println();
                        }
                    }
                }

                writer.println("SET FOREIGN_KEY_CHECKS = 1;");
                writer.println("-- FIM DO BACKUP");
                writer.flush();
            }

            // Compactar em arquivo .zip
            byte[] sqlBytes = baos.toByteArray();
            if (arquivoZipDestino.getParentFile() != null && !arquivoZipDestino.getParentFile().exists()) {
                arquivoZipDestino.getParentFile().mkdirs();
            }

            try (FileOutputStream fos = new FileOutputStream(arquivoZipDestino);
                 ZipOutputStream zos = new ZipOutputStream(fos)) {
                String nomeEntradaSql = arquivoZipDestino.getName().replace(".zip", ".sql");
                ZipEntry entry = new ZipEntry(nomeEntradaSql);
                zos.putNextEntry(entry);
                zos.write(sqlBytes);
                zos.closeEntry();
            }

            long tempoMs = System.currentTimeMillis() - inicio;
            long tamanhoKb = arquivoZipDestino.length() / 1024;
            return new ResultadoBackup(true, "Backup criado com sucesso!", arquivoZipDestino.getAbsolutePath(), totalTabelas, totalLinhas, tamanhoKb, tempoMs);

        } catch (Exception e) {
            System.err.println("[ERRO BACKUP] Falha ao realizar backup: " + e.getMessage());
            return new ResultadoBackup(false, "Erro ao gerar backup: " + e.getMessage(), null, 0, 0, 0, 0);
        }
    }

    public static ResultadoRestauracao restaurarBackup(File arquivoBackup) {
        if (!arquivoBackup.exists()) {
            return new ResultadoRestauracao(false, "Arquivo de backup não encontrado.");
        }

        try {
            String conteudoSql = "";
            if (arquivoBackup.getName().toLowerCase().endsWith(".zip")) {
                try (FileInputStream fis = new FileInputStream(arquivoBackup);
                     ZipInputStream zis = new ZipInputStream(fis)) {
                    ZipEntry entry;
                    while ((entry = zis.getNextEntry()) != null) {
                        if (entry.getName().toLowerCase().endsWith(".sql")) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            byte[] buffer = new byte[4096];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                baos.write(buffer, 0, len);
                            }
                            conteudoSql = baos.toString(StandardCharsets.UTF_8);
                            break;
                        }
                    }
                }
            } else {
                StringBuilder sb = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(arquivoBackup), StandardCharsets.UTF_8))) {
                    String linha;
                    while ((linha = reader.readLine()) != null) {
                        sb.append(linha).append("\n");
                    }
                }
                conteudoSql = sb.toString();
            }

            if (conteudoSql.trim().isEmpty()) {
                return new ResultadoRestauracao(false, "Nenhum comando SQL válido foi localizado dentro do backup.");
            }

            // Executar comandos de restauração
            try (Connection conn = ConnectionFactory.getConnection();
                 Statement stmt = conn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");

                // Limpa tabelas existentes antes de reinserir
                for (String t : TABELAS_ORDENADAS) {
                    try {
                        stmt.execute("DELETE FROM `" + t + "`");
                    } catch (SQLException ignored) {}
                }

                // Processar comandos linha por linha ou por ponto e vírgula
                String[] comandos = conteudoSql.split(";\\r?\\n");
                int executados = 0;
                for (String cmd : comandos) {
                    String limpo = cmd.trim();
                    if (!limpo.isEmpty() && !limpo.startsWith("--")) {
                        try {
                            stmt.execute(limpo);
                            executados++;
                        } catch (SQLException ex) {
                            System.err.println("[AVISO RESTAURAÇÃO] Falha no comando: " + ex.getMessage());
                        }
                    }
                }

                stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
                return new ResultadoRestauracao(true, "Banco de dados restaurado com sucesso! (" + executados + " comandos processados)");
            }

        } catch (Exception e) {
            System.err.println("[ERRO RESTAURAÇÃO] Falha ao restaurar backup: " + e.getMessage());
            return new ResultadoRestauracao(false, "Erro ao restaurar: " + e.getMessage());
        }
    }

    public static class ResultadoBackup {
        private final boolean sucesso;
        private final String mensagem;
        private final String caminhoArquivo;
        private final int totalTabelas;
        private final int totalRegistros;
        private final long tamanhoKb;
        private final long tempoMs;

        public ResultadoBackup(boolean sucesso, String mensagem, String caminhoArquivo, int totalTabelas, int totalRegistros, long tamanhoKb, long tempoMs) {
            this.sucesso = sucesso;
            this.mensagem = mensagem;
            this.caminhoArquivo = caminhoArquivo;
            this.totalTabelas = totalTabelas;
            this.totalRegistros = totalRegistros;
            this.tamanhoKb = tamanhoKb;
            this.tempoMs = tempoMs;
        }

        public boolean isSucesso() { return sucesso; }
        public String getMensagem() { return mensagem; }
        public String getCaminhoArquivo() { return caminhoArquivo; }
        public int getTotalTabelas() { return totalTabelas; }
        public int getTotalRegistros() { return totalRegistros; }
        public long getTamanhoKb() { return tamanhoKb; }
        public long getTempoMs() { return tempoMs; }
    }

    public static class ResultadoRestauracao {
        private final boolean sucesso;
        private final String mensagem;

        public ResultadoRestauracao(boolean sucesso, String mensagem) {
            this.sucesso = sucesso;
            this.mensagem = mensagem;
        }

        public boolean isSucesso() { return sucesso; }
        public String getMensagem() { return mensagem; }
    }
}
