package com.example.finditmobile;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finditmobile.adapter.NotificacaoAdapter;
import com.example.finditmobile.model.Notificacao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class NotificacoesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private NotificacaoAdapter adapter;
    private List<Notificacao> lista = new ArrayList<>();
    private FirebaseFirestore db;
    private String uid;
    private TextView tvVazio;

    @Override
    protected void onCreate(@Nullable Bundle s) {
        super.onCreate(s);
        setContentView(R.layout.activity_notificacoes);
        configurarToolbarEDrawer();

        recyclerView = findViewById(R.id.recyclerViewNotificacoes);
        tvVazio      = findViewById(R.id.tvVazio);

        db  = FirebaseFirestore.getInstance();
        uid = FirebaseAuth.getInstance().getUid();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NotificacaoAdapter(lista, new NotificacaoAdapter.OnActionListener() {
            @Override
            public void onToggleRead(Notificacao n, int position) {
                // marca/desmarca como lida
                db.collection("users")
                        .document(uid)
                        .collection("notificacoes")
                        .document(n.getId())
                        .update("lida", !n.isLida());
            }
            @Override
            public void onDelete(Notificacao n, int position) {
                // exclui
                db.collection("users")
                        .document(uid)
                        .collection("notificacoes")
                        .document(n.getId())
                        .delete();
            }
        });
        recyclerView.setAdapter(adapter);

        carregarNotificacoes();
    }

    private void carregarNotificacoes() {
        if (uid == null) {
            Toast.makeText(this, "Usuário não autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("users")
                .document(uid)
                .collection("notificacoes")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snaps, e) -> {
                    if (e != null) {
                        Toast.makeText(this, "Erro ao carregar notificações", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    lista.clear();
                    if (snaps != null && !snaps.isEmpty()) {
                        tvVazio.setVisibility(View.GONE);
                        for (DocumentSnapshot doc : snaps.getDocuments()) {
                            Notificacao n = doc.toObject(Notificacao.class);
                            n.setId(doc.getId());
                            lista.add(n);
                        }
                    } else {
                        tvVazio.setVisibility(View.VISIBLE);
                    }
                    adapter.notifyDataSetChanged();
                });
    }
}