package com.example.finditmobile;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AddItemActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private static final String PASTA_IMAGENS = "itens_perdidos_images";

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private Toolbar toolbar;

    private ImageView imageViewPreview;
    private TextInputEditText editTextTitulo;
    private TextInputEditText editTextDescricao;
    private Button buttonSalvar;
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

        escolherImagemLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                (ActivityResultCallback<Uri>) uri -> {
                    if (uri != null) {
                        imageUriSelecionada = uri;
                        imageViewPreview.setImageURI(uri);
                    }
                }
        );

        auth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this,
                    "Você precisa estar logado para acessar.",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        initViewsAndListeners();

        progressBar.setVisibility(View.VISIBLE);

        currentUser.getIdToken(false)
                .addOnSuccessListener(result -> {
                    progressBar.setVisibility(View.GONE);
                    Boolean isAdmin = (Boolean) result.getClaims().get("isAdmin");
                    if (isAdmin != null && isAdmin) {
                        // Pode prosseguir
                    } else {
                        Toast.makeText(this,
                                "Você não tem permissão para adicionar itens.",
                                Toast.LENGTH_LONG).show();
                        finish();
                    }
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Erro ao verificar permissões: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void initViewsAndListeners() {
        drawerLayout = findViewById(R.id.drawer_layout_add);
        navigationView = findViewById(R.id.navigation_view_add);
        navigationView.setNavigationItemSelectedListener(this);

        toolbar = findViewById(R.id.toolbar_add);
        setSupportActionBar(toolbar);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance()
                .getReference()
                .child(PASTA_IMAGENS);

        imageViewPreview = findViewById(R.id.imageViewPreview);
        editTextTitulo = findViewById(R.id.editTextTitulo);
        editTextDescricao = findViewById(R.id.editTextDescricao);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        progressBar = findViewById(R.id.progressBar);

        imageViewPreview.setOnClickListener(v ->
                escolherImagemLauncher.launch("image/*")
        );

        buttonSalvar.setOnClickListener(v -> salvarNovoItemComImagem());

        findViewById(R.id.openChatButton).setOnClickListener(v -> {
            Intent intent = new Intent(AddItemActivity.this, ChatbotActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
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
                });
    }

    private void salvarNovoItemComImagem() {
        String titulo = editTextTitulo.getText() != null
                ? editTextTitulo.getText().toString().trim() : "";
        String descricao = editTextDescricao.getText() != null
                ? editTextDescricao.getText().toString().trim() : "";

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
        if (imageUriSelecionada == null) {
            Toast.makeText(this,
                    "Selecione uma imagem para o item",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        progressBar.setVisibility(View.VISIBLE);

        String extensao = pegarExtensaoDoArquivo(imageUriSelecionada);
        String nomeArquivo = UUID.randomUUID() + "." + extensao;

        StorageReference imagemRef = storageReference.child(nomeArquivo);
        imagemRef.putFile(imageUriSelecionada)
                .addOnSuccessListener(taskSnapshot ->
                        imagemRef.getDownloadUrl()
                                .addOnSuccessListener(uriDownload -> {
                                    criarDocumentoItemPerdido(
                                            titulo,
                                            descricao,
                                            uriDownload.toString()
                                    );
                                })
                                .addOnFailureListener(e -> {
                                    progressBar.setVisibility(View.GONE);
                                    Toast.makeText(this,
                                            "Erro ao obter URL da imagem: " + e.getMessage(),
                                            Toast.LENGTH_LONG).show();
                                })
                )
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Falha no upload da imagem: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private String pegarExtensaoDoArquivo(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));
    }

    private void criarDocumentoItemPerdido(String titulo, String descricao, String downloadUrl) {
        Map<String, Object> novoItem = new HashMap<>();
        novoItem.put("titulo", titulo);
        novoItem.put("descricao", descricao);
        novoItem.put("imageUrl", downloadUrl);

        firestore.collection("itens_perdidos")
                .add(novoItem)
                .addOnSuccessListener(doc -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Item cadastrado com sucesso!",
                            Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(this,
                            "Erro ao cadastrar item: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    @Override
    public boolean onNavigationItemSelected(
            @NonNull android.view.MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }
}