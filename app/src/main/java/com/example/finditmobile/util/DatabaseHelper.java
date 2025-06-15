package com.example.finditmobile.util;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DatabaseHelper {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    public DatabaseHelper() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void cadastrarUsuarioFirebaseAuth(String nome, String email, String senha, String ra, String whatsapp, CadastroCallback callback) {
        mAuth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            // Dados adicionais para Firestore
                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("uid", uid);
                            userMap.put("nome", nome);
                            userMap.put("email", email);
                            userMap.put("ra", ra);
                            userMap.put("whatsapp", whatsapp);
                            userMap.put("createdAt", System.currentTimeMillis());

                            db.collection("users").document(uid)
                                    .set(userMap)
                                    .addOnSuccessListener(aVoid -> {
                                        callback.onSuccess();
                                    })
                                    .addOnFailureListener(callback::onFailure);
                        } else {
                            callback.onFailure(new Exception("Usuário não encontrado após cadastro"));
                        }
                    } else {
                        callback.onFailure(task.getException());
                    }
                });
    }
}
