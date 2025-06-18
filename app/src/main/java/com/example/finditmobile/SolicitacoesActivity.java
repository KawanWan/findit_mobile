package com.example.finditmobile;

import android.app.AlertDialog;
import android.content.Intent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finditmobile.adapter.SolicitacaoAdapter;
import com.example.finditmobile.model.Solicitacao;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SolicitacoesActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private SolicitacaoAdapter adapter;
    private List<Solicitacao> lista = new ArrayList<>();

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private boolean isAdmin;
    private TextView tvTitulo;

    @Override
    protected void onCreate(@Nullable android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitacoes);

        configurarToolbarEDrawer();
        tvTitulo = findViewById(R.id.tv_titulo_solicitacoes);

        firestore = FirebaseFirestore.getInstance();
        auth      = FirebaseAuth.getInstance();

        carregarSolicitacoes();

        FloatingActionButton chatFab = findViewById(R.id.openChatButton);
        chatFab.setOnClickListener(v -> {
            startActivity(new Intent(
                    SolicitacoesActivity.this,
                    ChatbotActivity.class
            ));
            overridePendingTransition(
                    R.anim.slide_in_right,
                    R.anim.slide_out_left
            );
        });

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
                }
        );
    }

    private void carregarSolicitacoes() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        user.getIdToken(true).addOnSuccessListener(result -> {
            isAdmin = Boolean.TRUE.equals(
                    result.getClaims().get("isAdmin")
            );

            tvTitulo.setText(
                    isAdmin ? "Todas as solicitações" : "Minhas Solicitações"
            );

            configurarRecyclerView();

            Query query = firestore.collection("solicitacoes");
            if (!isAdmin) {
                query = query.whereEqualTo("userId", user.getUid());
            }

            query.addSnapshotListener(new EventListener<QuerySnapshot>() {
                @Override
                public void onEvent(
                        @Nullable QuerySnapshot snaps,
                        @Nullable FirebaseFirestoreException e
                ) {
                    if (e != null) {
                        Toast.makeText(
                                SolicitacoesActivity.this,
                                "Erro ao carregar solicitações",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }
                    if (snaps != null) {
                        for (DocumentChange dc : snaps.getDocumentChanges()) {
                            Solicitacao s = dc.getDocument()
                                    .toObject(Solicitacao.class);
                            s.setId(dc.getDocument().getId());

                            switch (dc.getType()) {
                                case ADDED:
                                    lista.add(s);
                                    break;
                                case MODIFIED:
                                    for (int i = 0; i < lista.size(); i++) {
                                        if (lista.get(i).getId().equals(s.getId())) {
                                            lista.set(i, s);
                                            break;
                                        }
                                    }
                                    break;
                                case REMOVED:
                                    for (int i = 0; i < lista.size(); i++) {
                                        if (lista.get(i).getId().equals(s.getId())) {
                                            lista.remove(i);
                                            break;
                                        }
                                    }
                                    break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    }
                }
            });

        }).addOnFailureListener(e ->
                Toast.makeText(
                        this,
                        "Erro ao verificar permissão",
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    private void configurarRecyclerView() {
        recyclerView = findViewById(R.id.recyclerViewSolicitacoes);
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        adapter = new SolicitacaoAdapter(
                this,
                lista,
                isAdmin,

                // Listener para admin continua igual...
                (sol, novoStatus) -> {
                    View view = getLayoutInflater()
                            .inflate(R.layout.dialog_resposta_admin, null);
                    EditText etResp = view.findViewById(R.id.etResposta);

                    new AlertDialog.Builder(this)
                            .setTitle("Responder solicitação")
                            .setView(view)
                            .setPositiveButton("Enviar", (dlg, which) -> {
                                String texto = etResp.getText().toString().trim();
                                Map<String, Object> updates = new HashMap<>();
                                updates.put("status", novoStatus);
                                updates.put("respostaAdmin", texto);
                                firestore.collection("solicitacoes")
                                        .document(sol.getId())
                                        .update(updates)
                                        .addOnSuccessListener(a ->
                                                Toast.makeText(
                                                        this,
                                                        "Resposta enviada e status “" + novoStatus + "”",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                        )
                                        .addOnFailureListener(err ->
                                                Toast.makeText(
                                                        this,
                                                        "Erro ao atualizar solicitação",
                                                        Toast.LENGTH_SHORT
                                                ).show()
                                        );
                            })
                            .setNegativeButton("Cancelar", null)
                            .show();
                },

                // Listener para usuário comum com edição inline
                new SolicitacaoAdapter.OnUserActionListener() {
                    @Override
                    public void onEdit(Solicitacao s) {
                        // Edita mensagem via diálogo
                        final EditText et = new EditText(SolicitacoesActivity.this);
                        et.setText(s.getMensagem());
                        et.setSelection(et.getText().length());

                        new AlertDialog.Builder(SolicitacoesActivity.this)
                                .setTitle("Editar mensagem")
                                .setView(et)
                                .setPositiveButton("Salvar", (dlg, which) -> {
                                    String nova = et.getText().toString().trim();
                                    if (nova.isEmpty()) {
                                        Toast.makeText(
                                                SolicitacoesActivity.this,
                                                "Mensagem não pode ficar vazia",
                                                Toast.LENGTH_SHORT
                                        ).show();
                                        return;
                                    }
                                    firestore.collection("solicitacoes")
                                            .document(s.getId())
                                            .update("mensagem", nova)
                                            .addOnSuccessListener(a -> {
                                                s.setMensagem(nova);
                                                adapter.notifyDataSetChanged();
                                                Toast.makeText(
                                                        SolicitacoesActivity.this,
                                                        "Solicitação atualizada",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(
                                                            SolicitacoesActivity.this,
                                                            "Erro ao salvar alterações",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                })
                                .setNegativeButton("Cancelar", null)
                                .show();
                    }

                    @Override
                    public void onDelete(Solicitacao s) {
                        new AlertDialog.Builder(SolicitacoesActivity.this)
                                .setTitle("Excluir solicitação?")
                                .setMessage("Tem certeza de que deseja excluir esta solicitação?")
                                .setPositiveButton("Sim", (dlg, which) -> {
                                    firestore.collection("solicitacoes")
                                            .document(s.getId())
                                            .delete()
                                            .addOnSuccessListener(a -> {
                                                lista.remove(s);
                                                adapter.notifyDataSetChanged();
                                                Toast.makeText(
                                                        SolicitacoesActivity.this,
                                                        "Solicitação excluída",
                                                        Toast.LENGTH_SHORT
                                                ).show();
                                            })
                                            .addOnFailureListener(e ->
                                                    Toast.makeText(
                                                            SolicitacoesActivity.this,
                                                            "Erro ao excluir solicitação",
                                                            Toast.LENGTH_SHORT
                                                    ).show()
                                            );
                                })
                                .setNegativeButton("Não", null)
                                .show();
                    }
                }
        );
        recyclerView.setAdapter(adapter);
    }
}
