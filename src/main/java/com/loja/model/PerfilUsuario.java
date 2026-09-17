package com.loja.model;

public enum PerfilUsuario {
    ADMIN("Administrador", "Acesso total ao sistema, faturamento e configurações"),
    TECNICO("Técnico", "Acesso a Ordens de Serviço e Estoque de Peças"),
    ATENDENTE("Atendente / Caixa", "Acesso a Frente de Caixa, Abertura/Fechamento de Caixa, PDV, Clientes e OS");

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
        String v = valor.toUpperCase().trim();
        if (v.equals("CAIXA") || v.equals("OPERADOR") || v.equals("OPERADOR_CAIXA") || v.equals("BALCAO")) {
            return ATENDENTE;
        }
        try {
            return PerfilUsuario.valueOf(v);
        } catch (IllegalArgumentException e) {
            return ATENDENTE;
        }
    }

    @Override
    public String toString() {
        return nomeExibicao;
    }
}
