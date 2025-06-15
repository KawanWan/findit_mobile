package com.example.finditmobile.model;

public class ItemPerdido {
    private String id;
    private String titulo;
    private String descricao;
    private String imageUrl;

    // Construtor vazio (exigido pelo Firestore)
    public ItemPerdido() { }

    // Construtor completo (opcional, se quiser instanciar manualmente)
    public ItemPerdido(String id, String titulo, String descricao, String imageUrl) {
        this.id = id;
        this.titulo = titulo;
        this.descricao = descricao;
        this.imageUrl = imageUrl;
    }

    // Getters e setters
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
