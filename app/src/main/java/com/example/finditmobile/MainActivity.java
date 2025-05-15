package com.example.finditmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private Button openChatButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);

        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.END);
        });

        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                Toast.makeText(MainActivity.this, "Início selecionado", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.menu_about) {
                Toast.makeText(MainActivity.this, "Sobre selecionado", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.menu_login) {
                Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(loginIntent);
            } else if (id == R.id.menu_signup) {
                Intent signupIntent = new Intent(MainActivity.this, CadastroActivity.class);
                startActivity(signupIntent);
            } else if (id == R.id.menu_itens) {
                Intent signupIntent = new Intent(MainActivity.this, ItensActivity.class);
                startActivity(signupIntent);
            } else if (id == R.id.menu_solicitacoes) {
                Intent signupIntent = new Intent(MainActivity.this, SolicitacoesActivity.class);
                startActivity(signupIntent);
            } else if (id == R.id.menu_solicitar) {
                Intent signupIntent = new Intent(MainActivity.this, SolicitarActivity.class);
                startActivity(signupIntent);
            }

            return true;
        });

        openChatButton = findViewById(R.id.openChatButton);

        openChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, ChatbotActivity.class);
                startActivity(intent);
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}