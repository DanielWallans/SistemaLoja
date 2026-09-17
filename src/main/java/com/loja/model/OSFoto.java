package com.loja.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OSFoto {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private int osId;
    private String nomeArquivo;
    private String descricao;
    private byte[] dados;
    private byte[] miniatura;
    private long tamanhoBytes;
    private String operador;
    private LocalDateTime dataUpload;

    public OSFoto() {
        this.dataUpload = LocalDateTime.now();
    }

    public OSFoto(int osId, String nomeArquivo, String descricao, byte[] dados, byte[] miniatura,
                  long tamanhoBytes, String operador) {
        this.osId = osId;
        this.nomeArquivo = nomeArquivo;
        this.descricao = descricao;
        this.dados = dados;
        this.miniatura = miniatura;
        this.tamanhoBytes = tamanhoBytes;
        this.operador = operador;
        this.dataUpload = LocalDateTime.now();
    }

    public OSFoto(int id, int osId, String nomeArquivo, String descricao, byte[] dados, byte[] miniatura,
                  long tamanhoBytes, String operador, LocalDateTime dataUpload) {
        this.id = id;
        this.osId = osId;
        this.nomeArquivo = nomeArquivo;
        this.descricao = descricao;
        this.dados = dados;
        this.miniatura = miniatura;
        this.tamanhoBytes = tamanhoBytes;
        this.operador = operador;
        this.dataUpload = dataUpload != null ? dataUpload : LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOsId() { return osId; }
    public void setOsId(int osId) { this.osId = osId; }

    public String getNomeArquivo() { return nomeArquivo; }
    public void setNomeArquivo(String nomeArquivo) { this.nomeArquivo = nomeArquivo; }

    public String getDescricao() { return descricao != null ? descricao : ""; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public byte[] getDados() { return dados; }
    public void setDados(byte[] dados) { this.dados = dados; }

    public byte[] getMiniatura() { return miniatura; }
    public void setMiniatura(byte[] miniatura) { this.miniatura = miniatura; }

    public long getTamanhoBytes() { return tamanhoBytes; }
    public void setTamanhoBytes(long tamanhoBytes) { this.tamanhoBytes = tamanhoBytes; }

    public String getOperador() { return operador != null ? operador : "Técnico"; }
    public void setOperador(String operador) { this.operador = operador; }

    public LocalDateTime getDataUpload() { return dataUpload; }
    public void setDataUpload(LocalDateTime dataUpload) { this.dataUpload = dataUpload; }

    public String getDataUploadFormatada() {
        return dataUpload != null ? dataUpload.format(FORMATTER) : "-";
    }

    public String getTamanhoFormatado() {
        if (tamanhoBytes <= 0) return "0 KB";
        if (tamanhoBytes < 1024 * 1024) {
            return String.format("%.1f KB", tamanhoBytes / 1024.0);
        }
        return String.format("%.2f MB", tamanhoBytes / (1024.0 * 1024.0));
    }

    @Override
    public String toString() {
        return "OSFoto #" + id + " [" + nomeArquivo + "] - " + getDescricao();
    }
}
