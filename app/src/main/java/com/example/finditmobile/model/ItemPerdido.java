package com.example.finditmobile.model;

import androidx.annotation.Nullable;

public class ItemPerdido {
    private String id;
    private String titulo;
    private String descricao;
    @Nullable
    private String localizacao;
    private String imageUrl;
    private String ondeEncontrado;

    public ItemPerdido() { }

    public ItemPerdido(
            String id,
            String titulo,
            String descricao,
            String localizacao,
            String imageUrl,
            String ondeEncontrado
    ) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.localizacao = localizacao;
        this.imageUrl = imageUrl;
        this.ondeEncontrado = ondeEncontrado;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }
    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescricao() {
        return descricao;
    }
    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getLocalizacao() {
        return localizacao;
    }
    public void setLocalizacao(String localizacao) {
        this.localizacao = localizacao;
    }

    public String getImageUrl() {
        return imageUrl;
    }
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getOndeEncontrado() {
        return ondeEncontrado;
    }
    public void setOndeEncontrado(String ondeEncontrado) {
        this.ondeEncontrado = ondeEncontrado;
    }
}