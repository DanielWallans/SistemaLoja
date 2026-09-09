package com.loja.model;

public class SessaoUsuario {
    private static SessaoUsuario instancia;
    private Usuario usuarioLogado;

    private SessaoUsuario() {}

    public static synchronized SessaoUsuario getInstancia() {
        if (instancia == null) {
            instancia = new SessaoUsuario();
        }
        return instancia;
    }

    public Usuario getUsuarioLogado() {
        return usuarioLogado;
    }

    public void setUsuarioLogado(Usuario usuarioLogado) {
        this.usuarioLogado = usuarioLogado;
    }

    public boolean isAutenticado() {
        return usuarioLogado != null;
    }

    public void encerrarSessao() {
        this.usuarioLogado = null;
    }

    public boolean isAdmin() {
        return usuarioLogado != null && usuarioLogado.isAdmin();
    }

    public boolean isTecnico() {
        return usuarioLogado != null && usuarioLogado.isTecnico();
    }

    public boolean isAtendente() {
        return usuarioLogado != null && usuarioLogado.isAtendente();
    }

    public String getNomeUsuario() {
        return usuarioLogado != null ? usuarioLogado.getNome() : "Desconectado";
    }

    public String getPerfilNome() {
        return usuarioLogado != null && usuarioLogado.getPerfil() != null ? usuarioLogado.getPerfil().getNomeExibicao() : "-";
    }
}
