package com.loja.model;

public class ItemVenda {
    private int id;
    private int vendaId;
    private int produtoId;
    private String nomeProduto;
    private int quantidade;
    private double valorUnitario;
    private double subtotal;

    public ItemVenda(int produtoId, String nomeProduto, int quantidade, double valorUnitario) {
        this(0, 0, produtoId, nomeProduto, quantidade, valorUnitario, quantidade * valorUnitario);
    }

    public ItemVenda(int id, int vendaId, int produtoId, String nomeProduto, int quantidade, double valorUnitario, double subtotal) {
        this.id = id;
        this.vendaId = vendaId;
        this.produtoId = produtoId;
        this.nomeProduto = nomeProduto;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.subtotal = subtotal;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getVendaId() { return vendaId; }
    public void setVendaId(int vendaId) { this.vendaId = vendaId; }

    public int getProdutoId() { return produtoId; }
    public void setProdutoId(int produtoId) { this.produtoId = produtoId; }

    public String getNomeProduto() { return nomeProduto; }
    public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
        this.subtotal = this.quantidade * this.valorUnitario;
    }

    public double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(double valorUnitario) {
        this.valorUnitario = valorUnitario;
        this.subtotal = this.quantidade * this.valorUnitario;
    }

    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
}
