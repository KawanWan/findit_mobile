package com.example.finditmobile;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {

    private final FirebaseFirestore db;
    private final CollectionReference usuariosRef;

    public DatabaseHelper() {
        db = FirebaseFirestore.getInstance();
        usuariosRef = db.collection("usuarios");
    }

    public void cadastrarUsuario(String nome, String email, String senha, String ra, String whatsapp, OnCompleteListener<Void> callback) {
        usuariosRef.document(email).get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult().exists()) {
                // E-mail já está cadastrado
                callback.onComplete(Tasks.forException(new Exception("Email já cadastrado")));
            } else {
                // Pode cadastrar
                Map<String, Object> usuario = new HashMap<>();
                usuario.put("nome", nome);
                usuario.put("senha", senha);
                usuario.put("ra", ra);
                usuario.put("whatsapp", whatsapp);

                usuariosRef.document(email).set(usuario).addOnCompleteListener(callback);
            }
        });
    }


    public void verificarUsuario(String email, String senhaCriptografada, OnCompleteListener<DocumentSnapshot> callback) {
        usuariosRef.document(email).get().addOnCompleteListener(callback);
    }
}