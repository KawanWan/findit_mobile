package com.example.finditmobile;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddItemActivity extends BaseActivity {

    private static final String PASTA_IMAGENS = "itens_perdidos_images";

    private ImageView imageViewPreview;
    private TextInputEditText editTextTitulo;
    private TextInputEditText editTextDescricao;
    private TextInputEditText editTextLocation;
    private TextInputEditText editTextOndeEncontrado;
    private MaterialButton buttonSalvar;
    private ProgressBar progressBar;

    private Uri imageUriSelecionada;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;
    private FirebaseAuth auth;

    private ActivityResultLauncher<String> escolherImagemLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        // 1) configura toolbar e drawer herdados do BaseActivity
        configurarToolbarEDrawer();

        // 2) launcher de imagem
        escolherImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                (ActivityResultCallback<Uri>) uri -> {
                    if (uri != null) {
                        imageUriSelecionada = uri;
                        imageViewPreview.setImageURI(uri);
                    }
                }
        );

        // 3) verifica login e permissão admin
        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this,
                    "Você precisa estar logado para acessar.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        // 4) bind de views
        imageViewPreview       = findViewById(R.id.imageViewPreview);
        editTextTitulo         = findViewById(R.id.editTextTitulo);
        editTextDescricao      = findViewById(R.id.editTextDescricao);
        editTextLocation       = findViewById(R.id.editTextLocation);
        editTextOndeEncontrado = findViewById(R.id.editTextOndeEncontrado);
        buttonSalvar           = findViewById(R.id.buttonSalvar);
        progressBar            = findViewById(R.id.progressBar);

        imageViewPreview.setOnClickListener(v ->
                escolherImagemLauncher.launch("image/*")
        );

        buttonSalvar.setOnClickListener(v ->
                salvarNovoItemComImagem()
        );

        // 5) instâncias do Firestore e Storage
        firestore        = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance()
                .getReference()
                .child(PASTA_IMAGENS);

        // 6) checa permissão admin antes de prosseguir
        progressBar.setVisibility(ProgressBar.VISIBLE);
        currentUser.getIdToken(false)
                .addOnSuccessListener(result -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Boolean isAdmin = (Boolean) result.getClaims().get("isAdmin");
                    if (isAdmin == null || !isAdmin) {
                        Toast.makeText(this,
                                "Você não tem permissão para adicionar itens.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this,
                            "Erro ao verificar permissões: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void salvarNovoItemComImagem() {
        String titulo         = editTextTitulo.getText() != null
                ? editTextTitulo.getText().toString().trim() : "";
        String descricao      = editTextDescricao.getText() != null
                ? editTextDescricao.getText().toString().trim() : "";
        String location       = editTextLocation.getText() != null
                ? editTextLocation.getText().toString().trim() : "";
        String ondeEncontrado = editTextOndeEncontrado.getText() != null
                ? editTextOndeEncontrado.getText().toString().trim() : "";

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
        if (imageUriSelecionada == null) {
            Toast.makeText(this,
                    "Selecione uma imagem para o item",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);

        String extensao    = pegarExtensaoDoArquivo(imageUriSelecionada);
        String nomeArquivo = UUID.randomUUID() + "." + extensao;

        StorageReference imagemRef = storageReference.child(nomeArquivo);
        imagemRef.putFile(imageUriSelecionada)
                .addOnSuccessListener(task ->
                        imagemRef.getDownloadUrl()
                                .addOnSuccessListener(uri ->
                                        criarDocumentoItemPerdido(
                                                titulo, descricao,
                                                uri.toString(),
                                                location, ondeEncontrado
                                        )
                                )
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(ProgressBar.GONE);
                                    Toast.makeText(this,
                                            "Erro ao obter URL da imagem: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                })
                )
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this,
                            "Falha no upload da imagem: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void criarDocumentoItemPerdido(
            String titulo,
            String descricao,
            String downloadUrl,
            String location,
            String ondeEncontrado
    ) {
        Map<String, Object> novoItem = new HashMap<>();
        novoItem.put("titulo", titulo);
        novoItem.put("descricao", descricao);
        novoItem.put("imageUrl", downloadUrl);
        if (!location.isEmpty()) {
            novoItem.put("localizacao", location);
        }
        novoItem.put("ondeEncontrado", ondeEncontrado);

        firestore.collection("itens_perdidos")
                .add(novoItem)
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this,
                            "Item cadastrado com sucesso!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this,
                            "Erro ao cadastrar item: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private String pegarExtensaoDoArquivo(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }
}
