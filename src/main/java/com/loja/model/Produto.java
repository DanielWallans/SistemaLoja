package com.loja.model;

public class Produto {
    private int id;
    private String nome;
    private double preco;
    private int estoque;

    public Produto(int id, String nome, double preco, int estoque) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
    }

    // Se o estoque acabar, o sistema não quebra
    public boolean temEstoque(int quantidade) {
        return this.estoque >= quantidade;
    }

    public void baixarEstoque(int quantidade) {
        if (temEstoque(quantidade)) {
            this.estoque -= quantidade;
        }
    }

    // Getters rápidos
    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    public int getEstoque() { return estoque; }
}




