package com.loja.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CaixaSessao {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter FORMATTER_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FORMATTER_HORA = DateTimeFormatter.ofPattern("HH:mm");

    private int id;
    private String status; // "ABERTO", "FECHADO"
    private LocalDateTime dataAbertura;
    private LocalDateTime dataFechamento;
    private String operadorAbertura;
    private String operadorFechamento;
    private double saldoInicial;
    private double saldoFinalSistema;
    private double saldoFinalInformado;
    private double diferenca;
    private String justificativaDiferenca;
    private double fundoTrocoDeixado;
    private double sangriaMalote;

    // Totais apurados
    private double totalDinheiro;
    private double totalPix;
    private double totalDebitoBruto;
    private double totalDebitoLiquido;
    private double totalCreditoBruto;
    private double totalCreditoLiquido;
    private double totalSangrias;
    private double totalSuprimentos;
    private double totalVendasBruto;
    private double totalVendasLiquido;
    private int qtdVendas;
    private String contagemDetalhadaTexto;
    private String observacoes;

    public CaixaSessao() {
        this.status = "ABERTO";
        this.dataAbertura = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isAberta() {
        return "ABERTO".equalsIgnoreCase(status);
    }

    public LocalDateTime getDataAbertura() { return dataAbertura; }
    public void setDataAbertura(LocalDateTime dataAbertura) { this.dataAbertura = dataAbertura; }
    public String getDataAberturaFormatada() {
        return dataAbertura != null ? dataAbertura.format(FORMATTER) : "-";
    }
    public String getHoraAberturaFormatada() {
        return dataAbertura != null ? dataAbertura.format(FORMATTER_HORA) : "-";
    }

    public LocalDateTime getDataFechamento() { return dataFechamento; }
    public void setDataFechamento(LocalDateTime dataFechamento) { this.dataFechamento = dataFechamento; }
    public String getDataFechamentoFormatada() {
        return dataFechamento != null ? dataFechamento.format(FORMATTER) : "-";
    }
    public String getHoraFechamentoFormatada() {
        return dataFechamento != null ? dataFechamento.format(FORMATTER_HORA) : "-";
    }

    public String getOperadorAbertura() { return operadorAbertura; }
    public void setOperadorAbertura(String operadorAbertura) { this.operadorAbertura = operadorAbertura; }

    public String getOperadorFechamento() { return operadorFechamento; }
    public void setOperadorFechamento(String operadorFechamento) { this.operadorFechamento = operadorFechamento; }

    public double getSaldoInicial() { return saldoInicial; }
    public void setSaldoInicial(double saldoInicial) { this.saldoInicial = saldoInicial; }

    public double getSaldoFinalSistema() { return saldoFinalSistema; }
    public void setSaldoFinalSistema(double saldoFinalSistema) { this.saldoFinalSistema = saldoFinalSistema; }

    public double getSaldoFinalInformado() { return saldoFinalInformado; }
    public void setSaldoFinalInformado(double saldoFinalInformado) { this.saldoFinalInformado = saldoFinalInformado; }

    public double getDiferenca() { return diferenca; }
    public void setDiferenca(double diferenca) { this.diferenca = diferenca; }

    public String getJustificativaDiferenca() { return justificativaDiferenca; }
    public void setJustificativaDiferenca(String justificativaDiferenca) { this.justificativaDiferenca = justificativaDiferenca; }

    public double getFundoTrocoDeixado() { return fundoTrocoDeixado; }
    public void setFundoTrocoDeixado(double fundoTrocoDeixado) { this.fundoTrocoDeixado = fundoTrocoDeixado; }

    public double getSangriaMalote() { return sangriaMalote; }
    public void setSangriaMalote(double sangriaMalote) { this.sangriaMalote = sangriaMalote; }

    public double getTotalDinheiro() { return totalDinheiro; }
    public void setTotalDinheiro(double totalDinheiro) { this.totalDinheiro = totalDinheiro; }

    public double getTotalPix() { return totalPix; }
    public void setTotalPix(double totalPix) { this.totalPix = totalPix; }

    public double getTotalDebitoBruto() { return totalDebitoBruto; }
    public void setTotalDebitoBruto(double totalDebitoBruto) { this.totalDebitoBruto = totalDebitoBruto; }

    public double getTotalDebitoLiquido() { return totalDebitoLiquido; }
    public void setTotalDebitoLiquido(double totalDebitoLiquido) { this.totalDebitoLiquido = totalDebitoLiquido; }

    public double getTotalCreditoBruto() { return totalCreditoBruto; }
    public void setTotalCreditoBruto(double totalCreditoBruto) { this.totalCreditoBruto = totalCreditoBruto; }

    public double getTotalCreditoLiquido() { return totalCreditoLiquido; }
    public void setTotalCreditoLiquido(double totalCreditoLiquido) { this.totalCreditoLiquido = totalCreditoLiquido; }

    public double getTotalSangrias() { return totalSangrias; }
    public void setTotalSangrias(double totalSangrias) { this.totalSangrias = totalSangrias; }

    public double getTotalSuprimentos() { return totalSuprimentos; }
    public void setTotalSuprimentos(double totalSuprimentos) { this.totalSuprimentos = totalSuprimentos; }

    public double getTotalVendasBruto() { return totalVendasBruto; }
    public void setTotalVendasBruto(double totalVendasBruto) { this.totalVendasBruto = totalVendasBruto; }

    public double getTotalVendasLiquido() { return totalVendasLiquido; }
    public void setTotalVendasLiquido(double totalVendasLiquido) { this.totalVendasLiquido = totalVendasLiquido; }

    public int getQtdVendas() { return qtdVendas; }
    public void setQtdVendas(int qtdVendas) { this.qtdVendas = qtdVendas; }

    public String getContagemDetalhadaTexto() { return contagemDetalhadaTexto; }
    public void setContagemDetalhadaTexto(String contagemDetalhadaTexto) { this.contagemDetalhadaTexto = contagemDetalhadaTexto; }

    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }

    public String getStatusDiferencaFormatado() {
        if (Math.abs(diferenca) < 0.01) {
            return "🟢 Bateu Exato (R$ 0,00)";
        } else if (diferenca > 0) {
            return String.format("🟡 Sobra (+R$ %.2f)", diferenca);
        } else {
            return String.format("🔴 Quebra / Falta (-R$ %.2f)", Math.abs(diferenca));
        }
    }
}
