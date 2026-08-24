package com.loja.model;

import java.time.LocalDateTime;

public class OrdemServico {
    private int id;
    private int clienteId;
    private int equipamentoId;
    private String problemaRelatado;
    private String diagnosticoTecnico;
    private String status; // Aguardando Orçamento, Em Manutenção, Pronto, Entregue, Cancelado
    private double valorServico;
    private double valorTotal;
    private String checklistEntrada;
    private String observacoesFinais;
    private LocalDateTime dataEntrada;
    private LocalDateTime dataSaida;

    // Construtor para abertura básica
    public OrdemServico(int clienteId, int equipamentoId, String problemaRelatado) {
        this(clienteId, equipamentoId, problemaRelatado, "", "");
    }

    // Construtor para abertura com checklist e observações
    public OrdemServico(int clienteId, int equipamentoId, String problemaRelatado, String checklistEntrada, String observacoesFinais) {
        this.clienteId = clienteId;
        this.equipamentoId = equipamentoId;
        this.problemaRelatado = problemaRelatado;
        this.checklistEntrada = checklistEntrada;
        this.observacoesFinais = observacoesFinais;
        this.status = "Aguardando Orçamento";
        this.valorServico = 0.0;
        this.valorTotal = 0.0;
        this.dataEntrada = LocalDateTime.now();
    }

    // Construtor completo
    public OrdemServico(int id, int clienteId, int equipamentoId, String problemaRelatado,
                        String diagnosticoTecnico, String status, double valorServico, double valorTotal,
                        String checklistEntrada, String observacoesFinais,
                        LocalDateTime dataEntrada, LocalDateTime dataSaida) {
        this.id = id;
        this.clienteId = clienteId;
        this.equipamentoId = equipamentoId;
        this.problemaRelatado = problemaRelatado;
        this.diagnosticoTecnico = diagnosticoTecnico;
        this.status = status;
        this.valorServico = valorServico;
        this.valorTotal = valorTotal;
        this.checklistEntrada = checklistEntrada;
        this.observacoesFinais = observacoesFinais;
        this.dataEntrada = dataEntrada;
        this.dataSaida = dataSaida;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getClienteId() { return clienteId; }
    public void setClienteId(int clienteId) { this.clienteId = clienteId; }

    public int getEquipamentoId() { return equipamentoId; }
    public void setEquipamentoId(int equipamentoId) { this.equipamentoId = equipamentoId; }

    public String getProblemaRelatado() { return problemaRelatado; }
    public void setProblemaRelatado(String problemaRelatado) { this.problemaRelatado = problemaRelatado; }

    public String getDiagnosticoTecnico() { return diagnosticoTecnico; }
    public void setDiagnosticoTecnico(String diagnosticoTecnico) { this.diagnosticoTecnico = diagnosticoTecnico; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getValorServico() { return valorServico; }
    public void setValorServico(double valorServico) { this.valorServico = valorServico; }

    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }

    public String getChecklistEntrada() { return checklistEntrada; }
    public void setChecklistEntrada(String checklistEntrada) { this.checklistEntrada = checklistEntrada; }

    public String getObservacoesFinais() { return observacoesFinais; }
    public void setObservacoesFinais(String observacoesFinais) { this.observacoesFinais = observacoesFinais; }

    public LocalDateTime getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDateTime dataEntrada) { this.dataEntrada = dataEntrada; }

    public LocalDateTime getDataSaida() { return dataSaida; }
    public void setDataSaida(LocalDateTime dataSaida) { this.dataSaida = dataSaida; }
}
