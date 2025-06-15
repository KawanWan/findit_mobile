package com.example.finditmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finditmobile.adapter.ItemPerdidoAdapter;
import com.example.finditmobile.model.ItemPerdido;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class ItensActivity extends BaseActivity {

    private RecyclerView recyclerViewItens;
    private ItemPerdidoAdapter adapter;
    private List<ItemPerdido> lostItemList = new ArrayList<>();

    private FirebaseFirestore firestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itens);

        configurarToolbarEDrawer();
        configurarRecyclerView();

        firestore = FirebaseFirestore.getInstance();
        auth      = FirebaseAuth.getInstance();

        carregarItensPerdidos();
        verificarPermissaoAdmin();

        FloatingActionButton chatFab = findViewById(R.id.openChatButton);
        chatFab.setOnClickListener(v -> {
            startActivity(new Intent(ItensActivity.this, ChatbotActivity.class));
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
        });

        getOnBackPressedDispatcher()
                .addCallback(this, new OnBackPressedCallback(true) {
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
        recyclerViewItens = findViewById(R.id.recyclerViewItens);
        if (recyclerViewItens == null) {
            Toast.makeText(this, "RecyclerView não encontrado!", Toast.LENGTH_LONG).show();
            return;
        }
        recyclerViewItens.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewItens.setItemAnimator(new DefaultItemAnimator());
        adapter = new ItemPerdidoAdapter(this, lostItemList);
        recyclerViewItens.setAdapter(adapter);
    }

    private void verificarPermissaoAdmin() {
        FloatingActionButton fabAdd = findViewById(R.id.fab_add_item);
        fabAdd.setVisibility(View.GONE);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            currentUser.getIdToken(true)
                    .addOnSuccessListener(result -> {
                        Boolean isAdmin = (Boolean) result.getClaims().get("isAdmin");
                        if (Boolean.TRUE.equals(isAdmin)) {
                            fabAdd.setVisibility(View.VISIBLE);
                            adapter.setIsAdmin(true);
                            fabAdd.setOnClickListener(v ->
                                    startActivity(new Intent(ItensActivity.this, AddItemActivity.class))
                            );
                        } else {
                            adapter.setIsAdmin(false);
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Erro ao verificar permissão", Toast.LENGTH_SHORT).show()
                    );
        } else {
            adapter.setIsAdmin(false);
        }
    }

    private void carregarItensPerdidos() {
        firestore.collection("itens_perdidos")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(ItensActivity.this,
                                    "Erro ao carregar itens", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        if (snapshots != null) {
                            for (DocumentChange dc : snapshots.getDocumentChanges()) {
                                ItemPerdido item = dc.getDocument()
                                        .toObject(ItemPerdido.class);
                                item.setId(dc.getDocument().getId());
                                switch (dc.getType()) {
                                    case ADDED:
                                        if (indexPorId(item.getId()) == -1) {
                                            lostItemList.add(item);
                                            adapter.notifyItemInserted(lostItemList.size() - 1);
                                        }
                                        break;
                                    case MODIFIED:
                                        int idxMod = indexPorId(item.getId());
                                        if (idxMod != -1) {
                                            lostItemList.set(idxMod, item);
                                            adapter.notifyItemChanged(idxMod);
                                        }
                                        break;
                                    case REMOVED:
                                        int idxRem = indexPorId(item.getId());
                                        if (idxRem != -1) {
                                            lostItemList.remove(idxRem);
                                            adapter.notifyItemRemoved(idxRem);
                                        }
                                        break;
                                }
                            }
                        }
                    }
                });
    }

    private int indexPorId(String id) {
        for (int i = 0; i < lostItemList.size(); i++) {
            if (lostItemList.get(i).getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }

}