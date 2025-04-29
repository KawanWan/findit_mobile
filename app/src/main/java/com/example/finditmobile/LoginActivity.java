package com.example.finditmobile;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.finditmobile.util.HashUtil;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, senhaEditText;
    private Button loginButton;
    private TextView esqueciSenhaTextView, cadastroLinkTextView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailEditText = findViewById(R.id.email);
        senhaEditText = findViewById(R.id.senha);
        loginButton = findViewById(R.id.login_btn);
        esqueciSenhaTextView = findViewById(R.id.esqueci_senha);
        cadastroLinkTextView = findViewById(R.id.extra_links);

        dbHelper = new DatabaseHelper(this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loginUsuario();
            }
        });

        cadastroLinkTextView.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });

        esqueciSenhaTextView.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this, "Função de recuperação de senha ainda não implementada!", Toast.LENGTH_SHORT).show();
        });
    }

    private void loginUsuario() {
        String email = emailEditText.getText().toString().trim();
        String senha = senhaEditText.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Criptografar a senha digitada para comparar
        String senhaCriptografada = HashUtil.sha256(senha);

        // Consultar o banco de dados
        boolean usuarioValido = dbHelper.verificarUsuario(email, senhaCriptografada);

        if (usuarioValido) {
            Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(this, "Email ou senha incorretos!", Toast.LENGTH_SHORT).show();
        }
    }
}