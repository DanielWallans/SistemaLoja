package com.loja.model;

public enum PerfilUsuario {
    ADMIN("Administrador", "Acesso total ao sistema, faturamento e configurações"),
    TECNICO("Técnico", "Acesso a Ordens de Serviço e Estoque de Peças"),
    ATENDENTE("Atendente / Balcão", "Acesso a Frente de Caixa, PDV, Clientes e Abertura de OS");

    private final String nomeExibicao;
    private final String descricao;

    PerfilUsuario(String nomeExibicao, String descricao) {
        this.nomeExibicao = nomeExibicao;
        this.descricao = descricao;
    }

    public String getNomeExibicao() {
        return nomeExibicao;
    }

    public String getDescricao() {
        return descricao;
    }

    public static PerfilUsuario fromString(String valor) {
        if (valor == null) return ATENDENTE;
        try {
            return PerfilUsuario.valueOf(valor.toUpperCase().trim());
        } catch (IllegalArgumentException e) {
            return ATENDENTE;
        }
    }

    @Override
    public String toString() {
        return nomeExibicao;
    }
}
