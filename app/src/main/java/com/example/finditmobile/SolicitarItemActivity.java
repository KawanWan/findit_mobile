package com.example.finditmobile;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.finditmobile.model.ItemPerdido;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

        itemId = getIntent().getStringExtra("itemId");

        FirebaseFirestore.getInstance()
                .collection("itens_perdidos")
                .document(itemId)
                .get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot doc) {
                        if (doc.exists()) {
                            ItemPerdido item = doc.toObject(ItemPerdido.class);

                            tvTitulo.setText(item.getTitulo());
                            tvDescricao.setText(item.getDescricao());

                            String url = item.getImageUrl();
                            if (url != null && !url.isEmpty()) {
                                Glide.with(SolicitarItemActivity.this)
                                        .load(url)
                                        .placeholder(R.drawable.ic_baseline_image_24)
                                        .into(ivImagem);
                            } else {
                                ivImagem.setImageResource(R.drawable.ic_baseline_image_24);
                            }
                        } else {
                            Toast.makeText(SolicitarItemActivity.this,
                                    "Item não encontrado", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(SolicitarItemActivity.this,
                                "Erro ao carregar item", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });

        // 4. Envio da solicitação
        btnEnviar.setOnClickListener(v -> enviarSolicitacao());
    }

    private void enviarSolicitacao() {
        String mensagem = edtMensagem.getText().toString().trim();
        if (mensagem.isEmpty()) {
            edtMensagem.setError("Informe uma mensagem");
            return;
        }

        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("solicitacoes")
                .whereEqualTo("itemId", itemId)
                .whereEqualTo("userId", uid)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        Toast.makeText(this,
                                "Você já solicitou este item anteriormente.",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Map<String, Object> solic = new HashMap<>();
                        solic.put("itemId", itemId);
                        solic.put("userId", uid);
                        solic.put("mensagem", mensagem);
                        solic.put("status", "pendente");
                        solic.put("timestamp", FieldValue.serverTimestamp());

                        db.collection("solicitacoes")
                                .add(solic)
                                .addOnSuccessListener(ref -> {
                                    Toast.makeText(this,
                                            "Solicitação enviada!",
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this,
                                                "Erro ao enviar solicitação",
                                                Toast.LENGTH_SHORT).show()
                                );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Erro ao verificar solicitações anteriores",
                                Toast.LENGTH_SHORT).show()
                );
    }
}