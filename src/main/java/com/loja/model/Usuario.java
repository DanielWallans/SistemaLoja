package com.loja.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Usuario {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private int id;
    private String nome;
    private String login;
    private String senhaHash;
    private PerfilUsuario perfil;
    private boolean ativo;
    private LocalDateTime dataCadastro;

    public Usuario() {
        this.perfil = PerfilUsuario.ATENDENTE;
        this.ativo = true;
        this.dataCadastro = LocalDateTime.now();
    }

    public Usuario(int id, String nome, String login, String senhaHash, PerfilUsuario perfil, boolean ativo, LocalDateTime dataCadastro) {
        this.id = id;
        this.nome = nome;
        this.login = login;
        this.senhaHash = senhaHash;
        this.perfil = perfil != null ? perfil : PerfilUsuario.ATENDENTE;
        this.ativo = ativo;
        this.dataCadastro = dataCadastro != null ? dataCadastro : LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public PerfilUsuario getPerfil() { return perfil; }
    public void setPerfil(PerfilUsuario perfil) { this.perfil = perfil; }

    public boolean isAtivo() { return ativo; }
    public void setAtivo(boolean ativo) { this.ativo = ativo; }

    public LocalDateTime getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(LocalDateTime dataCadastro) { this.dataCadastro = dataCadastro; }

    public String getDataCadastroFormatada() {
        return dataCadastro != null ? dataCadastro.format(FORMATTER) : "-";
    }

    public boolean isAdmin() {
        return perfil == PerfilUsuario.ADMIN;
    }

    public boolean isTecnico() {
        return perfil == PerfilUsuario.TECNICO;
    }

    public boolean isAtendente() {
        return perfil == PerfilUsuario.ATENDENTE;
    }

    public boolean isCaixa() {
        return perfil == PerfilUsuario.ATENDENTE;
    }

    public String getStatusFormatado() {
        return ativo ? "Ativo" : "Inativo";
    }

    @Override
    public String toString() {
        return nome + " (" + (perfil != null ? perfil.getNomeExibicao() : "Usuário") + ")";
    }
}
