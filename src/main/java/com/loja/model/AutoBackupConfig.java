package com.loja.model;

import java.io.File;

public class AutoBackupConfig {
    private boolean ativo = true;
    private boolean salvarPosOperacaoCritica = true; // Salvar logo após vendas e OS (Anti-apagão)
    private boolean salvarAoFechar = true;           // Salvar ao encerrar o sistema
    private boolean salvarPeriodico = true;          // Salvar em intervalos regulares
    private int intervaloMinutos = 15;               // Padrão: 15 minutos
    private String pastaDestino;
    private String pastaSecundaria = "";            // Pendrive ou Nuvem (opcional)
    private int retencaoMaxArquivos = 20;            // Reter últimos X arquivos históricos
    private boolean lembreteInicial = true;          // Lembrete ao iniciar o sistema se backup estiver desatualizado
    private String ultimoBackupTimestamp = "";
    private String ultimoBackupStatus = "";

    public AutoBackupConfig() {
        // Pasta padrão: Documents/SystemPro/Backups
        File userHome = new File(System.getProperty("user.home", "."));
        File docs = new File(userHome, "Documents");
        if (!docs.exists()) {
            docs = userHome;
        }
        File pastaPadrao = new File(new File(docs, "SystemPro"), "Backups");
        this.pastaDestino = pastaPadrao.getAbsolutePath();
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isSalvarPosOperacaoCritica() {
        return salvarPosOperacaoCritica;
    }

    public void setSalvarPosOperacaoCritica(boolean salvarPosOperacaoCritica) {
        this.salvarPosOperacaoCritica = salvarPosOperacaoCritica;
    }

    public boolean isSalvarAoFechar() {
        return salvarAoFechar;
    }

    public void setSalvarAoFechar(boolean salvarAoFechar) {
        this.salvarAoFechar = salvarAoFechar;
    }

    public boolean isSalvarPeriodico() {
        return salvarPeriodico;
    }

    public void setSalvarPeriodico(boolean salvarPeriodico) {
        this.salvarPeriodico = salvarPeriodico;
    }

    public int getIntervaloMinutos() {
        return intervaloMinutos;
    }

    public void setIntervaloMinutos(int intervaloMinutos) {
        if (intervaloMinutos < 1) intervaloMinutos = 1;
        this.intervaloMinutos = intervaloMinutos;
    }

    public String getPastaDestino() {
        return pastaDestino;
    }

    public void setPastaDestino(String pastaDestino) {
        this.pastaDestino = pastaDestino;
    }

    public String getPastaSecundaria() {
        return pastaSecundaria != null ? pastaSecundaria : "";
    }

    public void setPastaSecundaria(String pastaSecundaria) {
        this.pastaSecundaria = pastaSecundaria != null ? pastaSecundaria.trim() : "";
    }

    public int getRetencaoMaxArquivos() {
        return retencaoMaxArquivos;
    }

    public void setRetencaoMaxArquivos(int retencaoMaxArquivos) {
        if (retencaoMaxArquivos < 3) retencaoMaxArquivos = 3;
        this.retencaoMaxArquivos = retencaoMaxArquivos;
    }

    public boolean isLembreteInicial() {
        return lembreteInicial;
    }

    public void setLembreteInicial(boolean lembreteInicial) {
        this.lembreteInicial = lembreteInicial;
    }

    public String getUltimoBackupTimestamp() {
        return ultimoBackupTimestamp != null ? ultimoBackupTimestamp : "";
    }

    public void setUltimoBackupTimestamp(String ultimoBackupTimestamp) {
        this.ultimoBackupTimestamp = ultimoBackupTimestamp;
    }

    public String getUltimoBackupStatus() {
        return ultimoBackupStatus != null ? ultimoBackupStatus : "";
    }

    public void setUltimoBackupStatus(String ultimoBackupStatus) {
        this.ultimoBackupStatus = ultimoBackupStatus;
    }
}
