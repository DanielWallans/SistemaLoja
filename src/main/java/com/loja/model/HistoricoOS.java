package com.loja.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HistoricoOS {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final DateTimeFormatter FORMATTER_COMPACTO = DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private int id;
    private int osId;
    private String status;
    private LocalDateTime dataHora;
    private String observacao;

    public HistoricoOS(int osId, String status, LocalDateTime dataHora, String observacao) {
        this(0, osId, status, dataHora, observacao);
    }

    public HistoricoOS(int id, int osId, String status, LocalDateTime dataHora, String observacao) {
        this.id = id;
        this.osId = osId;
        this.status = status;
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
        this.observacao = observacao != null ? observacao : "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getOsId() {
        return osId;
    }

    public void setOsId(int osId) {
        this.osId = osId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getDataHora() {
        return dataHora;
    }

    public void setDataHora(LocalDateTime dataHora) {
        this.dataHora = dataHora;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getDataHoraFormatada() {
        return dataHora != null ? dataHora.format(FORMATTER) : "-";
    }

    public String getDataHoraCompacta() {
        return dataHora != null ? dataHora.format(FORMATTER_COMPACTO) : "-";
    }

    @Override
    public String toString() {
        return String.format("[%s] %s - %s", getDataHoraFormatada(), status, observacao);
    }
}
