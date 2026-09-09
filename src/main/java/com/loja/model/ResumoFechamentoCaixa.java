package com.loja.model;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ResumoFechamentoCaixa {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private LocalDate data;
    private double saldoAtualGaveta;
    private double totalDinheiro;
    private double totalPix;
    private double totalDebitoBruto;
    private double totalDebitoLiquido;
    private double totalCreditoBruto;
    private double totalCreditoLiquido;
    private double totalSangrias;
    private double totalSuprimentos;
    private double totalGeralVendasBruto;
    private double totalGeralVendasLiquido;
    private int quantidadeVendas;
    private List<CaixaMovimento> movimentacoes = new ArrayList<>();

    public ResumoFechamentoCaixa(LocalDate data) {
        this.data = data != null ? data : LocalDate.now();
    }

    public LocalDate getData() { return data; }
    public String getDataFormatada() { return data != null ? data.format(FORMATTER) : "-"; }

    public double getSaldoAtualGaveta() { return saldoAtualGaveta; }
    public void setSaldoAtualGaveta(double saldoAtualGaveta) { this.saldoAtualGaveta = saldoAtualGaveta; }

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

    public double getTotalGeralVendasBruto() { return totalGeralVendasBruto; }
    public void setTotalGeralVendasBruto(double totalGeralVendasBruto) { this.totalGeralVendasBruto = totalGeralVendasBruto; }

    public double getTotalGeralVendasLiquido() { return totalGeralVendasLiquido; }
    public void setTotalGeralVendasLiquido(double totalGeralVendasLiquido) { this.totalGeralVendasLiquido = totalGeralVendasLiquido; }

    public int getQuantidadeVendas() { return quantidadeVendas; }
    public void setQuantidadeVendas(int quantidadeVendas) { this.quantidadeVendas = quantidadeVendas; }

    public List<CaixaMovimento> getMovimentacoes() { return movimentacoes; }
    public void setMovimentacoes(List<CaixaMovimento> movimentacoes) { this.movimentacoes = movimentacoes; }
}
