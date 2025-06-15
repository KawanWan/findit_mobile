package com.example.finditmobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected DrawerLayout drawerLayout;
    protected NavigationView navigationView;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void configurarToolbarEDrawer() {
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        configurarMenuNavigation();
        navigationView.setNavigationItemSelectedListener(this);
    }

    private void configurarMenuNavigation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        View headerView = navigationView.getHeaderView(0);
        TextView userName = headerView.findViewById(R.id.textViewUserName);
        TextView userEmail = headerView.findViewById(R.id.textViewEmail);

        if (currentUser != null) {
            String nome = currentUser.getDisplayName();
            String email = currentUser.getEmail();

            userName.setText(nome != null ? nome : "Usuário FindIt");
            userEmail.setText(email != null ? email : "Email não disponível");

            navigationView.getMenu().findItem(R.id.menu_login).setVisible(false);
            navigationView.getMenu().findItem(R.id.menu_signup).setVisible(false);
            navigationView.getMenu().findItem(R.id.menu_logout).setVisible(true);
        } else {
            userName.setText("Bem-vindo");
            userEmail.setText("findit@facens.com");

            navigationView.getMenu().findItem(R.id.menu_logout).setVisible(false);
            navigationView.getMenu().findItem(R.id.menu_login).setVisible(true);
            navigationView.getMenu().findItem(R.id.menu_signup).setVisible(true);
        }
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        if (id == R.id.menu_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.menu_itens) {
            startActivity(new Intent(this, ItensActivity.class));
        } else if (id == R.id.menu_solicitar) {
            startActivity(new Intent(this, SolicitarItemActivity.class));
        } else if (id == R.id.menu_solicitacoes) {
            startActivity(new Intent(this, SolicitacoesActivity.class));
        } else if (id == R.id.menu_about) {
            Toast.makeText(this, "Sobre selecionado", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.menu_login) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (id == R.id.menu_signup) {
            startActivity(new Intent(this, CadastroActivity.class));
        } else if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logout realizado", Toast.LENGTH_SHORT).show();
            recreate();
        }

        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout != null && drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}