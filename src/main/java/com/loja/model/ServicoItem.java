package com.loja.model;

public class ServicoItem {
    private int id;
    private int osId;
    private String descricao;
    private double valor;

    public ServicoItem(String descricao, double valor) {
        this.descricao = descricao;
        this.valor = valor;
    }

    public ServicoItem(int id, int osId, String descricao, double valor) {
        this.id = id;
        this.osId = osId;
        this.descricao = descricao;
        this.valor = valor;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getOsId() { return osId; }
    public void setOsId(int osId) { this.osId = osId; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
}
