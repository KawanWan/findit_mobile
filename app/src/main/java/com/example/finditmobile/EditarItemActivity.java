package com.example.finditmobile;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.bumptech.glide.Glide;
import com.example.finditmobile.model.ItemPerdido;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class EditarItemActivity extends BaseActivity {

    private TextInputEditText editTextTitulo;
    private TextInputEditText editTextDescricao;
    private TextInputEditText editTextLocalizacao;
    private TextInputEditText editTextOndeEncontrado;
    private MaterialButton buttonSalvar;
    private ImageView imageViewPreview;
    private ProgressBar progressBar;

    private Uri imageUriSelecionada = null;
    private String imageUrlAtual = null;
    private String itemId;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private ActivityResultLauncher<String> escolherImagemLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_item);

        // 1) configura toolbar e drawer
        configurarToolbarEDrawer();

        // 2) bind views
        editTextTitulo         = findViewById(R.id.editTextTituloEdit);
        editTextDescricao      = findViewById(R.id.editTextDescricaoEdit);
        editTextLocalizacao    = findViewById(R.id.editTextLocalizacaoEdit);
        editTextOndeEncontrado = findViewById(R.id.editTextOndeEncontradoEdit);
        buttonSalvar           = findViewById(R.id.buttonSalvarEdit);
        imageViewPreview       = findViewById(R.id.imageViewPreviewEdit);
        progressBar            = findViewById(R.id.progressBar);

        // 3) Firestore & Storage
        firestore        = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance()
                .getReference("itens_perdidos_images");

        // 4) escolha de imagem
        escolherImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                (ActivityResultCallback<Uri>) uri -> {
                    if (uri != null) {
                        imageUriSelecionada = uri;
                        imageViewPreview.setImageURI(uri);
                    }
                }
        );

        // 5) pega itemId da Intent
        itemId = getIntent().getStringExtra("itemId");
        if (itemId == null || itemId.isEmpty()) {
            Toast.makeText(this, "Item inválido", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        carregarDadosItem();

        imageViewPreview.setOnClickListener(v ->
                escolherImagemLauncher.launch("image/*")
        );
        buttonSalvar.setOnClickListener(v -> salvarAlteracoes());
    }

    private void carregarDadosItem() {
        progressBar.setVisibility(ProgressBar.VISIBLE);

        firestore.collection("itens_perdidos")
                .document(itemId)
                .get()
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    if (!doc.exists()) {
                        Toast.makeText(this, "Item não encontrado", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    ItemPerdido item = doc.toObject(ItemPerdido.class);
                    if (item != null) {
                        editTextTitulo.setText(item.getTitulo());
                        editTextDescricao.setText(item.getDescricao());
                        editTextLocalizacao.setText(item.getLocalizacao());
                        editTextOndeEncontrado.setText(item.getOndeEncontrado());
                        imageUrlAtual = item.getImageUrl();

                        if (imageUrlAtual != null && !imageUrlAtual.isEmpty()) {
                            Glide.with(this)
                                    .load(imageUrlAtual)
                                    .centerCrop()
                                    .into(imageViewPreview);
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Erro ao carregar dados", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void salvarAlteracoes() {
        String titulo         = editTextTitulo.getText().toString().trim();
        String descricao      = editTextDescricao.getText().toString().trim();
        String location       = editTextLocalizacao.getText().toString().trim();
        String ondeEncontrado = editTextOndeEncontrado.getText().toString().trim();

        // validações
        if (titulo.isEmpty()) {
            editTextTitulo.setError("Informe um título");
            editTextTitulo.requestFocus();
            return;
        }
        if (descricao.isEmpty()) {
            editTextDescricao.setError("Informe uma descrição");
            editTextDescricao.requestFocus();
            return;
        }
        if (ondeEncontrado.isEmpty()) {
            editTextOndeEncontrado.setError("Informe onde foi encontrado");
            editTextOndeEncontrado.requestFocus();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);

        if (imageUriSelecionada != null) {
            String nomeArquivo = UUID.randomUUID().toString();
            StorageReference ref = storageReference.child(nomeArquivo);
            ref.putFile(imageUriSelecionada)
                    .addOnSuccessListener(task -> ref.getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                removerImagemAntiga();
                                atualizarFirestore(
                                        titulo, descricao,
                                        uri.toString(), location,
                                        ondeEncontrado
                                );
                            })
                    )
                    .addOnFailureListener(e -> {
                        progressBar.setVisibility(ProgressBar.GONE);
                        Toast.makeText(this, "Erro ao enviar imagem", Toast.LENGTH_SHORT).show();
                    });
        } else {
            atualizarFirestore(
                    titulo, descricao,
                    imageUrlAtual, location,
                    ondeEncontrado
            );
        }
    }

    private void atualizarFirestore(
            String titulo,
            String descricao,
            String imageUrl,
            String location,
            String ondeEncontrado
    ) {
        firestore.collection("itens_perdidos")
                .document(itemId)
                .update(
                        "titulo", titulo,
                        "descricao", descricao,
                        "imageUrl", imageUrl,
                        "localizacao", location,
                        "ondeEncontrado", ondeEncontrado
                )
                .addOnSuccessListener(v -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Item atualizado com sucesso", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Erro ao atualizar item", Toast.LENGTH_SHORT).show();
                });
    }

    private void removerImagemAntiga() {
        if (imageUrlAtual != null && !imageUrlAtual.isEmpty()) {
            FirebaseStorage.getInstance()
                    .getReferenceFromUrl(imageUrlAtual)
                    .delete();
        }
    }
}