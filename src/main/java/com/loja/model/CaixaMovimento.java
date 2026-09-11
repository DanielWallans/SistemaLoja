package com.loja.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CaixaMovimento {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private int id;
    private String tipo; // SUPRIMENTO, SANGRIA, VENDA_PDV, RECEBIMENTO_OS
    private String modalidade; // DINHEIRO, PIX, DEBITO, CREDITO
    private double valor;
    private double taxa;
    private double valorLiquido;
    private String justificativa;
    private Integer osId;
    private Integer vendaId;
    private LocalDateTime dataHora;

    public CaixaMovimento(String tipo, String modalidade, double valor, double taxa, double valorLiquido, 
                          String justificativa, Integer osId, Integer vendaId) {
        this(0, tipo, modalidade, valor, taxa, valorLiquido, justificativa, osId, vendaId, LocalDateTime.now());
    }

    public CaixaMovimento(int id, String tipo, String modalidade, double valor, double taxa, double valorLiquido, 
                          String justificativa, Integer osId, Integer vendaId, LocalDateTime dataHora) {
        this.id = id;
        this.tipo = tipo;
        this.modalidade = modalidade != null ? modalidade : "DINHEIRO";
        this.valor = valor;
        this.taxa = taxa;
        this.valorLiquido = valorLiquido;
        this.justificativa = justificativa != null ? justificativa : "";
        this.osId = osId;
        this.vendaId = vendaId;
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getModalidade() { return modalidade; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }

    public double getTaxa() { return taxa; }
    public void setTaxa(double taxa) { this.taxa = taxa; }

    public double getValorLiquido() { return valorLiquido; }
    public void setValorLiquido(double valorLiquido) { this.valorLiquido = valorLiquido; }

    public String getJustificativa() { return justificativa; }
    public void setJustificativa(String justificativa) { this.justificativa = justificativa; }

    public Integer getOsId() { return osId; }
    public void setOsId(Integer osId) { this.osId = osId; }

    public Integer getVendaId() { return vendaId; }
    public void setVendaId(Integer vendaId) { this.vendaId = vendaId; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getDataHoraFormatada() {
        return dataHora != null ? dataHora.format(FORMATTER) : "-";
    }

    public String getTipoFormatado() {
        if ("SANGRIA".equalsIgnoreCase(tipo)) return "Sangria (Retirada)";
        if ("SUPRIMENTO".equalsIgnoreCase(tipo)) return "Suprimento (Troco)";
        if ("VENDA_PDV".equalsIgnoreCase(tipo)) return "Venda PDV";
        if ("RECEBIMENTO_OS".equalsIgnoreCase(tipo)) return "Recebimento de OS";
        return tipo;
    }
}
