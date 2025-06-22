package com.example.finditmobile;

import android.content.ContentResolver;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddItemActivity extends BaseActivity {

    private static final String PASTA_IMAGENS = "itens_perdidos_images";
    private static final int REQUEST_CAMERA_PERMISSION = 100;

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
    private ActivityResultLauncher<Uri> capturarImagemLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);

        configurarToolbarEDrawer();

        escolherImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUriSelecionada = uri;
                        imageViewPreview.setImageURI(uri);
                    }
                }
        );

        capturarImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                resultado -> {
                    if (resultado) {
                        imageViewPreview.setImageURI(imageUriSelecionada);
                    }
                }
        );

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Você precisa estar logado para acessar.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        imageViewPreview = findViewById(R.id.imageViewPreview);
        editTextTitulo = findViewById(R.id.editTextTitulo);
        editTextDescricao = findViewById(R.id.editTextDescricao);
        editTextLocation = findViewById(R.id.editTextLocation);
        editTextOndeEncontrado = findViewById(R.id.editTextOndeEncontrado);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        progressBar = findViewById(R.id.progressBar);

        imageViewPreview.setOnClickListener(v -> abrirSeletorImagem());

        buttonSalvar.setOnClickListener(v -> salvarNovoItemComImagem());

        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference().child(PASTA_IMAGENS);

        progressBar.setVisibility(ProgressBar.VISIBLE);
        currentUser.getIdToken(false)
                .addOnSuccessListener(result -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Boolean isAdmin = (Boolean) result.getClaims().get("isAdmin");
                    if (isAdmin == null || !isAdmin) {
                        Toast.makeText(this, "Você não tem permissão para adicionar itens.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Erro ao verificar permissões: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void abrirSeletorImagem() {
        String[] opcoes = {"Tirar foto", "Escolher da galeria"};

        new android.app.AlertDialog.Builder(this)
                .setTitle("Selecionar imagem")
                .setItems(opcoes, (dialog, which) -> {
                    if (which == 0) {
                        capturarImagem();
                    } else {
                        escolherImagemLauncher.launch("image/*");
                    }
                })
                .show();
    }

    private void capturarImagem() {
        if (checkSelfPermission(android.Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            executarCapturaImagem();
        } else {
            requestPermissions(new String[]{android.Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
        }
    }

    private void executarCapturaImagem() {
        String nomeArquivo = UUID.randomUUID().toString() + ".jpg";
        File arquivo = new File(getCacheDir(), nomeArquivo);
        imageUriSelecionada = androidx.core.content.FileProvider.getUriForFile(
                this,
                getPackageName() + ".fileprovider",
                arquivo
        );
        capturarImagemLauncher.launch(imageUriSelecionada);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                executarCapturaImagem();
            } else {
                Toast.makeText(this, "Permissão da câmera negada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void salvarNovoItemComImagem() {
        String titulo = editTextTitulo.getText() != null ? editTextTitulo.getText().toString().trim() : "";
        String descricao = editTextDescricao.getText() != null ? editTextDescricao.getText().toString().trim() : "";
        String location = editTextLocation.getText() != null ? editTextLocation.getText().toString().trim() : "";
        String ondeEncontrado = editTextOndeEncontrado.getText() != null ? editTextOndeEncontrado.getText().toString().trim() : "";

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
            Toast.makeText(this, "Selecione uma imagem para o item", Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(ProgressBar.VISIBLE);

        String extensao = pegarExtensaoDoArquivo(imageUriSelecionada);
        String nomeArquivo = UUID.randomUUID() + "." + extensao;

        StorageReference imagemRef = storageReference.child(nomeArquivo);
        imagemRef.putFile(imageUriSelecionada)
                .addOnSuccessListener(task -> imagemRef.getDownloadUrl()
                        .addOnSuccessListener(uri -> criarDocumentoItemPerdido(
                                titulo, descricao, uri.toString(), location, ondeEncontrado
                        ))
                        .addOnFailureListener(e -> {
                            progressBar.setVisibility(ProgressBar.GONE);
                            Toast.makeText(this, "Erro ao obter URL da imagem: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }))
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Falha no upload da imagem: " + e.getMessage(), Toast.LENGTH_LONG).show();
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
                    Toast.makeText(this, "Item cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    Toast.makeText(this, "Erro ao cadastrar item: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private String pegarExtensaoDoArquivo(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        String tipo = cr.getType(uri);
        if (tipo == null) return "jpg";
        String extensao = mime.getExtensionFromMimeType(tipo);
        return extensao != null ? extensao : "jpg";
    }
}