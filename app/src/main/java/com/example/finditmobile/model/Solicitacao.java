package com.example.finditmobile.model;

import com.google.firebase.firestore.ServerTimestamp;
import java.util.Date;

public class Solicitacao {
    private String id;
    private String itemId;
    private String userId;
    private String mensagem;
    private String status;
    private String respostaAdmin;

    @ServerTimestamp
    private Date timestamp;

    public Solicitacao() { }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getItemId() {
        return itemId;
    }
    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public String getUserId() {
        return userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getMensagem() {
        return mensagem;
    }
    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getRespostaAdmin() {
        return respostaAdmin;
    }
    public void setRespostaAdmin(String respostaAdmin) {
        this.respostaAdmin = respostaAdmin;
    }

    public Date getTimestamp() {
        return timestamp;
    }
    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}