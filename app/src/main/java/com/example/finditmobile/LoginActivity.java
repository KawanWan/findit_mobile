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
import com.google.firebase.firestore.DocumentSnapshot;

public class LoginActivity extends AppCompatActivity {

    private EditText emailEditText, senhaEditText;
    private Button loginButton;
    private TextView esqueciSenhaTextView, cadastroLinkTextView;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Inicializa os componentes da UI
        initViews();

        dbHelper = new DatabaseHelper();

        loginButton.setOnClickListener(view -> loginUsuario());

        cadastroLinkTextView.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, CadastroActivity.class));
        });

        esqueciSenhaTextView.setOnClickListener(v -> {
            Toast.makeText(this, "Função de recuperação de senha ainda não implementada!", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        emailEditText = findViewById(R.id.email);
        senhaEditText = findViewById(R.id.senha);
        loginButton = findViewById(R.id.login_btn);
        esqueciSenhaTextView = findViewById(R.id.esqueci_senha);
        cadastroLinkTextView = findViewById(R.id.extra_links);
    }

    private void loginUsuario() {
        String email = emailEditText.getText().toString().trim();
        String senha = senhaEditText.getText().toString().trim();

        if (!validarCampos(email, senha)) return;

        String senhaCriptografada = HashUtil.sha256(senha);

        dbHelper.verificarUsuario(email, senhaCriptografada, task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document != null && document.exists()) {
                    String senhaSalva = document.getString("senha");

                    if (senhaSalva != null && senhaSalva.equals(senhaCriptografada)) {
                        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        Toast.makeText(this, "Email ou senha incorretos!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Usuário não encontrado!", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Erro ao acessar o banco de dados!", Toast.LENGTH_SHORT).show();
            }
        });
    }


    private boolean validarCampos(String email, String senha) {
        if (email.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email inválido!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }
}
