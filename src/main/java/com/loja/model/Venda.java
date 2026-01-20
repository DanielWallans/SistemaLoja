package com.loja.model;

import java.time.LocalDateTime;

public class Venda {
    private int produtoId;
    private int quantidade;
    private double valorTotal;
    private LocalDateTime dataHora;

    public Venda(int produtoId, int quantidade, double valorTotal) {
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.valorTotal = valorTotal;
        this.dataHora = LocalDateTime.now();
    }


    public int getProdutoId() { return produtoId; }
    public int getQuantidade() { return quantidade; }
    public double getValorTotal() { return valorTotal; }
    public LocalDateTime getDataHora() { return dataHora; }
}