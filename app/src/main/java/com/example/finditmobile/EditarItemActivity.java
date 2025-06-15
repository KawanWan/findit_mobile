package com.example.finditmobile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.finditmobile.model.ItemPerdido;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EditarItemActivity extends AppCompatActivity {

    private EditText editTextTitulo, editTextDescricao;
    private Button buttonSalvar;
    private ImageView imageViewPreview;
    private ProgressBar progressBar;

    private Uri imageUriSelecionada = null;
    private String imageUrlAtual = null;
    private String itemId;

    private FirebaseFirestore firestore;
    private FirebaseStorage storage;

    private ActivityResultLauncher<String> escolherImagemLauncher;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_item);

        editTextTitulo = findViewById(R.id.editTextTituloEdit);
        editTextDescricao = findViewById(R.id.editTextDescricaoEdit);
        buttonSalvar = findViewById(R.id.buttonSalvarEdit);
        imageViewPreview = findViewById(R.id.imageViewPreviewEdit);
        progressBar = findViewById(R.id.progressBar);

        firestore = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();

        escolherImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUriSelecionada = uri;
                        imageViewPreview.setImageURI(uri);
                    }
                }
        );

        itemId = getIntent().getStringExtra("itemId");
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Item inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        carregarDadosItem();

        imageViewPreview.setOnClickListener(v -> escolherImagemLauncher.launch("image/*"));

        buttonSalvar.setOnClickListener(v -> salvarAlteracoes());
    }

    private void carregarDadosItem() {
        progressBar.setVisibility(View.VISIBLE);

        firestore.collection("itens_perdidos")
                .document(itemId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    progressBar.setVisibility(View.GONE);
                    if (documentSnapshot.exists()) {
                        ItemPerdido item = documentSnapshot.toObject(ItemPerdido.class);
                        if (item != null) {
                            editTextTitulo.setText(item.getTitulo());
                            editTextDescricao.setText(item.getDescricao());
                            imageUrlAtual = item.getImageUrl();

                            if (imageUrlAtual != null && !imageUrlAtual.isEmpty()) {
                                Glide.with(this)
                                        .load(imageUrlAtual)
                                        .centerCrop()
                                        .into(imageViewPreview);
                            }
                        }
                    } else {
                        Toast.makeText(this, "Item não encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void salvarAlteracoes() {
        String titulo = editTextTitulo.getText().toString().trim();
        String descricao = editTextDescricao.getText().toString().trim();

        if (titulo.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        if (imageUriSelecionada != null) {
            StorageReference storageRef = storage.getReference("itens_perdidos_images/" + UUID.randomUUID());
            storageRef.putFile(imageUriSelecionada)
                    .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                String novaImageUrl = uri.toString();
                                removerImagemAntiga();
                                atualizarFirestore(titulo, descricao, novaImageUrl);
                            }))
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(this, "Erro ao enviar imagem", Toast.LENGTH_SHORT).show();
                    });
        } else {
            atualizarFirestore(titulo, descricao, imageUrlAtual);
        }
    }

    private void atualizarFirestore(String titulo, String descricao, String imageUrl) {
        firestore.collection("itens_perdidos").document(itemId)
                .update(
                        "titulo", titulo,
                        "descricao", descricao,
                        "imageUrl", imageUrl
                )
                .addOnSuccessListener(aVoid -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Item atualizado com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this, "Erro ao atualizar item", Toast.LENGTH_SHORT).show();
                });
    }

    private void removerImagemAntiga() {
        if (imageUrlAtual != null && !imageUrlAtual.isEmpty()) {
            StorageReference antigaRef = storage.getReferenceFromUrl(imageUrlAtual);
            antigaRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        // Imagem antiga deletada com sucesso
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Erro ao remover imagem antiga", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}