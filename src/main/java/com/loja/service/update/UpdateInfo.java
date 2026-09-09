package com.loja.service.update;

public class UpdateInfo {
    private final String versao;
    private final String data;
    private final String novidades;
    private final String downloadUrl;

    public UpdateInfo(String versao, String data, String novidades, String downloadUrl) {
        this.versao = versao;
        this.data = data;
        this.novidades = novidades;
        this.downloadUrl = downloadUrl;
    }

    public String getVersao() {
        return versao;
    }

    public String getData() {
        return data;
    }

    public String getNovidades() {
        return novidades;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }
}
