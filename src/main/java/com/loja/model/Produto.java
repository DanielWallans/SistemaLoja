package com.loja.model;

public class Produto {
    private int id;
    private String codigoBarras;
    private String nome;
    private double preco;
    private int estoque;

    public Produto(int id, String nome, double preco, int estoque) {
        this(id, "", nome, preco, estoque);
    }

    public Produto(int id, String codigoBarras, String nome, double preco, int estoque) {
        this.id = id;
        this.codigoBarras = codigoBarras != null ? codigoBarras : "";
        this.nome = nome;
        this.preco = preco;
        this.estoque = estoque;
    }

    public boolean temEstoque(int quantidade) {
        return this.estoque >= quantidade;
    }

    public void baixarEstoque(int quantidade) {
        if (temEstoque(quantidade)) {
            this.estoque -= quantidade;
        }
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCodigoBarras() {
        return codigoBarras;
    }

    public void setCodigoBarras(String codigoBarras) {
        this.codigoBarras = codigoBarras;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public double getPreco() {
        return preco;
    }

    public void setPreco(double preco) {
        this.preco = preco;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    @Override
    public String toString() {
        return (codigoBarras != null && !codigoBarras.isEmpty() ? "[" + codigoBarras + "] " : "#" + id + " - ") + nome + " (R$ " + String.format("%.2f", preco) + ")";
    }
}




