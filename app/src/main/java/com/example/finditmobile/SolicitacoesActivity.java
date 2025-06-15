package com.example.finditmobile;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finditmobile.adapter.SolicitacaoAdapter;
import com.example.finditmobile.model.Solicitacao;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class SolicitacoesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private SolicitacaoAdapter adapter;
    private List<Solicitacao> lista = new ArrayList<>();

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitacoes);

        configurarToolbarEDrawer();
        configurarRecyclerView();

        firestore = FirebaseFirestore.getInstance();
        auth      = FirebaseAuth.getInstance();

        carregarSolicitacoes();

        FloatingActionButton chatFab = findViewById(R.id.openChatButton);
        chatFab.setOnClickListener(v -> {
            startActivity(new Intent(SolicitacoesActivity.this, ChatbotActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        // Fecha drawer antes de sair
        getOnBackPressedDispatcher().addCallback(this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START);
                        } else {
                            setEnabled(false);
                            onBackPressed();
                        }
                    }
                });
    }

    private void configurarRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewSolicitacoes);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        adapter = new SolicitacaoAdapter(this, lista);
        recyclerView.setAdapter(adapter);
    }

    private void carregarSolicitacoes() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        user.getIdToken(true).addOnSuccessListener(result -> {
            boolean isAdmin = Boolean.TRUE.equals(result.getClaims().get("isAdmin"));

            Query query = firestore.collection("solicitacoes");
            if (!isAdmin) {
                query = query.whereEqualTo("userId", user.getUid());
            }

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(@Nullable QuerySnapshot snaps,
                                    @Nullable FirebaseFirestoreException e) {
                    if (e != null) {
                        Toast.makeText(SolicitacoesActivity.this,
                                "Erro ao carregar solicitações", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (snaps != null) {
                        lista.clear();
                        for (DocumentChange dc : snaps.getDocumentChanges()) {
                            Solicitacao s = dc.getDocument()
                                    .toObject(Solicitacao.class);
                            s.setId(dc.getDocument().getId());
                            lista.add(s);
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });

        }).addOnFailureListener(e ->
                Toast.makeText(this,
                        "Erro ao verificar permissão", Toast.LENGTH_SHORT).show()
        );
    }
}