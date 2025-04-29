package com.example.finditmobile;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.finditmobile.util.HashUtil;
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

        nomeEditText = findViewById(R.id.nome);
        emailEditText = findViewById(R.id.email);
        senhaEditText = findViewById(R.id.senha);
        confirmSenhaEditText = findViewById(R.id.confirm_senha);
        raEditText = findViewById(R.id.ra);
        whatsappEditText = findViewById(R.id.whatsapp);
        MaskEditText.insertPhoneMask(whatsappEditText);
        cadastrarBtn = findViewById(R.id.cadastrar_btn);
        extraLinks = findViewById(R.id.extra_links);

        dbHelper = new DatabaseHelper(this);

        cadastrarBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cadastrarUsuario();
            }
        });

        extraLinks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void cadastrarUsuario() {
        String nome = nomeEditText.getText().toString().trim();
        String email = emailEditText.getText().toString().trim();
        String senha = senhaEditText.getText().toString().trim();
        String confirmSenha = confirmSenhaEditText.getText().toString().trim();
        String ra = raEditText.getText().toString().trim();
        String whatsapp = whatsappEditText.getText().toString().trim();

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

        String senhaCriptografada = HashUtil.sha256(senha);

        boolean sucesso = dbHelper.cadastrarUsuario(nome, email, senhaCriptografada, ra, whatsapp);

        if (sucesso) {
            Toast.makeText(this, "Cadastro realizado com sucesso!", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(CadastroActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Erro ao cadastrar. Verifique se o email já foi usado!", Toast.LENGTH_LONG).show();
        }
    }
    private boolean isValidPhoneNumber(String phone) {
        phone = phone.replaceAll("[^\\d]", "");

        return phone.length() >= 10 && phone.length() <= 11;
    }
}