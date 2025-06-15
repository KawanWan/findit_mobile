package com.example.finditmobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finditmobile.util.CadastroCallback;
import com.example.finditmobile.util.DatabaseHelper;
import com.example.finditmobile.util.MaskEditText;

public class CadastroActivity extends AppCompatActivity {

    private EditText nomeEditText, emailEditText, senhaEditText, confirmSenhaEditText, raEditText, whatsappEditText;
    private Button cadastrarBtn;
    private TextView extraLinks;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro);

        // Inicializar campos
        nomeEditText = findViewById(R.id.nome);
        emailEditText = findViewById(R.id.email);
        senhaEditText = findViewById(R.id.senha);
        confirmSenhaEditText = findViewById(R.id.confirm_senha);
        raEditText = findViewById(R.id.ra);
        whatsappEditText = findViewById(R.id.whatsapp);
        MaskEditText.insertPhoneMask(whatsappEditText); // Máscara opcional

        cadastrarBtn = findViewById(R.id.cadastrar_btn);
        extraLinks = findViewById(R.id.extra_links);

        dbHelper = new DatabaseHelper();

        // Ação do botão cadastrar
        cadastrarBtn.setOnClickListener(v -> cadastrarUsuario());

        // Link para login
        extraLinks.setOnClickListener(v -> {
            startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void cadastrarUsuario() {
        String nome = nomeEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String senha = senhaEditText.getText().toString().trim();
        String confirmSenha = confirmSenhaEditText.getText().toString().trim();
        String ra = raEditText.getText().toString().trim();
        String whatsapp = whatsappEditText.getText().toString().trim();

        // Validação
        if (TextUtils.isEmpty(nome) || TextUtils.isEmpty(email) || TextUtils.isEmpty(senha) ||
                TextUtils.isEmpty(confirmSenha) || TextUtils.isEmpty(ra) || TextUtils.isEmpty(whatsapp)) {
            Toast.makeText(this, "Por favor, preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!isValidPhoneNumber(whatsapp)) {
            Toast.makeText(this, "Número de WhatsApp inválido!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!senha.equals(confirmSenha)) {
            Toast.makeText(this, "As senhas não conferem!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() < 6) {
            Toast.makeText(this, "A senha deve ter pelo menos 6 caracteres!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Chamada para helper
        dbHelper.cadastrarUsuarioFirebaseAuth(nome, email, senha, ra, whatsapp, new CadastroCallback() {
            @Override
            public void onSuccess() {
                runOnUiThread(() -> {
                    Toast.makeText(CadastroActivity.this, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(CadastroActivity.this, LoginActivity.class));
                    finish();
                });
            }

            @Override
            public void onFailure(Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(CadastroActivity.this, "Erro ao cadastrar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private boolean isValidPhoneNumber(String phone) {
        phone = phone.replaceAll("[^\\d]", "");
        return phone.length() >= 10 && phone.length() <= 11;
    }
}