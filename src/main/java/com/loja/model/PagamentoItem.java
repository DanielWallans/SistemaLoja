package com.loja.model;

import java.time.LocalDateTime;

public class PagamentoItem {
    private int id;
    private Integer vendaId;
    private Integer osId;
    private String modalidade; // DINHEIRO, PIX, DEBITO, CREDITO_1X, CREDITO_PARCELADO
    private double valorBruto;
    private double taxaPercentual;
    private double valorTaxa;
    private double valorLiquido;
    private int parcelas;
    private double valorRecebidoCliente; // para troco em dinheiro
    private double troco;
    private LocalDateTime dataHora;

    public PagamentoItem(String modalidade, double valorBruto, double taxaPercentual, int parcelas) {
        this(0, null, null, modalidade, valorBruto, taxaPercentual, parcelas, LocalDateTime.now());
    }

    public PagamentoItem(int id, Integer vendaId, Integer osId, String modalidade, double valorBruto, 
                         double taxaPercentual, int parcelas, LocalDateTime dataHora) {
        this.id = id;
        this.vendaId = vendaId;
        this.osId = osId;
        this.modalidade = modalidade != null ? modalidade : "DINHEIRO";
        this.valorBruto = valorBruto;
        this.taxaPercentual = taxaPercentual;
        this.valorTaxa = (valorBruto * taxaPercentual) / 100.0;
        this.valorLiquido = valorBruto - this.valorTaxa;
        this.parcelas = Math.max(1, parcelas);
        this.valorRecebidoCliente = valorBruto;
        this.troco = 0.0;
        this.dataHora = dataHora != null ? dataHora : LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Integer getVendaId() { return vendaId; }
    public void setVendaId(Integer vendaId) { this.vendaId = vendaId; }

    public Integer getOsId() { return osId; }
    public void setOsId(Integer osId) { this.osId = osId; }

    public String getModalidade() { return modalidade; }
    public void setModalidade(String modalidade) { this.modalidade = modalidade; }

    public double getValorBruto() { return valorBruto; }
    public void setValorBruto(double valorBruto) {
        this.valorBruto = valorBruto;
        this.valorTaxa = (this.valorBruto * this.taxaPercentual) / 100.0;
        this.valorLiquido = this.valorBruto - this.valorTaxa;
    }

    public double getTaxaPercentual() { return taxaPercentual; }
    public void setTaxaPercentual(double taxaPercentual) {
        this.taxaPercentual = taxaPercentual;
        this.valorTaxa = (this.valorBruto * this.taxaPercentual) / 100.0;
        this.valorLiquido = this.valorBruto - this.valorTaxa;
    }

    public double getValorTaxa() { return valorTaxa; }
    public double getValorLiquido() { return valorLiquido; }

    public int getParcelas() { return parcelas; }
    public void setParcelas(int parcelas) { this.parcelas = Math.max(1, parcelas); }

    public double getValorRecebidoCliente() { return valorRecebidoCliente; }
    public void setValorRecebidoCliente(double valorRecebidoCliente) {
        this.valorRecebidoCliente = valorRecebidoCliente;
        this.troco = Math.max(0.0, this.valorRecebidoCliente - this.valorBruto);
    }

    public double getTroco() { return troco; }
    public void setTroco(double troco) { this.troco = troco; }

    public LocalDateTime getDataHora() { return dataHora; }
    public void setDataHora(LocalDateTime dataHora) { this.dataHora = dataHora; }

    public String getModalidadeFormatada() {
        if ("DINHEIRO".equalsIgnoreCase(modalidade)) return "💵 Dinheiro";
        if ("PIX".equalsIgnoreCase(modalidade)) return "📱 PIX";
        if ("DEBITO".equalsIgnoreCase(modalidade)) return "💳 Cartão de Débito";
        if (modalidade != null && modalidade.contains("CREDITO")) {
            return parcelas > 1 ? String.format("💳 Cartão de Crédito (%dx)", parcelas) : "💳 Cartão de Crédito (1x)";
        }
        return modalidade;
    }
}
