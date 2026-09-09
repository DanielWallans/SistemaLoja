package com.loja.model;

public class Equipamento {
    private int id;
    private int clienteId;
    private String tipo; // Computador, Notebook, Impressora, Celular, Tablet, Monitor, Outro
    private String marca;
    private String modelo;
    private String numeroSerie;
    private String cor;
    private String avarias; // Marcas de uso, avarias visíveis
    private String patrimonio;
    private String senhaAcesso;
    private String acessorios; // Lista de acessórios deixados

    // Construtor básico legado
    public Equipamento(int clienteId, String tipo, String marca, String modelo, String numeroSerie) {
        this(0, clienteId, tipo, marca, modelo, numeroSerie, "", "", "", "", "");
    }

    // Construtor completo para novos cadastros
    public Equipamento(int clienteId, String tipo, String marca, String modelo, String numeroSerie,
                       String cor, String avarias, String patrimonio, String senhaAcesso, String acessorios) {
        this(0, clienteId, tipo, marca, modelo, numeroSerie, cor, avarias, patrimonio, senhaAcesso, acessorios);
    }

    // Construtor completo
    public Equipamento(int id, int clienteId, String tipo, String marca, String modelo, String numeroSerie,
                       String cor, String avarias, String patrimonio, String senhaAcesso, String acessorios) {
        this.id = id;
        this.clienteId = clienteId;
        this.tipo = tipo;
        this.marca = marca;
        this.modelo = modelo;
        this.numeroSerie = numeroSerie;
        this.cor = cor;
        this.avarias = avarias;
        this.patrimonio = patrimonio;
        this.senhaAcesso = senhaAcesso;
        this.acessorios = acessorios;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getMarca() { return marca; }
    public void setMarca(String marca) { this.marca = marca; }

    public String getModelo() { return modelo; }
    public void setModelo(String modelo) { this.modelo = modelo; }

    public String getNumeroSerie() { return numeroSerie; }
    public void setNumeroSerie(String numeroSerie) { this.numeroSerie = numeroSerie; }

    public String getCor() { return cor; }
    public void setCor(String cor) { this.cor = cor; }

    public String getAvarias() { return avarias; }
    public void setAvarias(String avarias) { this.avarias = avarias; }

    public String getPatrimonio() { return patrimonio; }
    public void setPatrimonio(String patrimonio) { this.patrimonio = patrimonio; }

    public String getSenhaAcesso() { return senhaAcesso; }
    public void setSenhaAcesso(String senhaAcesso) { this.senhaAcesso = senhaAcesso; }

    public String getAcessorios() { return acessorios; }
    public void setAcessorios(String acessorios) { this.acessorios = acessorios; }

    public String getDescricaoCompleta() {
        return (tipo != null ? tipo : "Equipamento") + " " + (marca != null ? marca : "") + " " + (modelo != null ? modelo : "");
    }

    @Override
    public String toString() {
        return "#" + id + " - " + getDescricaoCompleta();
    }
}
