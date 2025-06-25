package com.example.finditmobile;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.example.finditmobile.model.ItemPerdido;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class SolicitarItemActivity extends BaseActivity {
    private String itemId;
    private ImageView ivImagem;
    private TextView tvTitulo, tvDescricao;
    private EditText edtMensagem;
    private MaterialButton btnEnviar;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_solicitar_item);
        configurarToolbarEDrawer();

        ivImagem    = findViewById(R.id.iv_item_imagem);
        tvTitulo    = findViewById(R.id.tv_item_titulo);
        tvDescricao = findViewById(R.id.tv_item_descricao);
        edtMensagem = findViewById(R.id.edt_mensagem);
        btnEnviar   = findViewById(R.id.btn_solicitar);

        db     = FirebaseFirestore.getInstance();
        itemId = getIntent().getStringExtra("itemId");

        carregarItem();
        btnEnviar.setOnClickListener(v -> enviarSolicitacao());
    }

    private void carregarItem() {
        db.collection("itens_perdidos")
                .document(itemId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Item não encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    ItemPerdido item = doc.toObject(ItemPerdido.class);
                    tvTitulo.setText(item.getTitulo());
                    tvDescricao.setText(item.getDescricao());
                    String url = item.getImageUrl();
                    if (url != null && !url.isEmpty()) {
                        Glide.with(this)
                                .load(url)
                                .placeholder(R.drawable.ic_baseline_image_24)
                                .into(ivImagem);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Erro ao carregar item", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void enviarSolicitacao() {
        String mensagem = edtMensagem.getText().toString().trim();
        if (mensagem.isEmpty()) {
            edtMensagem.setError("Informe uma mensagem");
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();

        // 1) Consulta pra ver se já existe
        db.collection("solicitacoes")
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        // Já existe pelo menos uma solicitação
                        Toast.makeText(this, "Você já solicitou este item.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 2) Não existe: cria a solicitação
                    Map<String, Object> solic = new HashMap<>();
                    solic.put("itemId",    itemId);
                    solic.put("userId",    uid);
                    solic.put("mensagem",  mensagem);
                    solic.put("status",    "pendente");
                    solic.put("timestamp", FieldValue.serverTimestamp());

                    db.collection("solicitacoes")
                            .add(solic)
                            .addOnSuccessListener(ref -> {
                                Toast.makeText(this, "Solicitação enviada!", Toast.LENGTH_SHORT).show();
                                finish();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Erro ao enviar solicitação", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Erro ao verificar solicitações", Toast.LENGTH_SHORT).show()
                );
    }
}