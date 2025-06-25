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
import com.google.firebase.firestore.FirebaseFirestore;

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

        if (drawerLayout != null && navigationView != null) {
            ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                    this, drawerLayout, toolbar,
                    R.string.navigation_drawer_open,
                    R.string.navigation_drawer_close
            );
            drawerLayout.addDrawerListener(toggle);
            toggle.syncState();

            configurarMenuNavigation();
            navigationView.setNavigationItemSelectedListener(this);

            atualizarBadgeNotificacoes();
        }
    }

    private void configurarMenuNavigation() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (navigationView == null) return;

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

    private void atualizarBadgeNotificacoes() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (navigationView == null) return;

        if (currentUser == null) return;

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(currentUser.getUid())
                .collection("notificacoes")
                .whereEqualTo("lida", false)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    int count = queryDocumentSnapshots.size();
                    if (count > 0) {
                        View actionView = navigationView.getMenu()
                                .findItem(R.id.menu_notificacoes)
                                .setActionView(R.layout.menu_badge_layout)
                                .getActionView();

                        TextView badgeCounter = actionView.findViewById(R.id.badge_counter);
                        badgeCounter.setText(String.valueOf(count));
                        badgeCounter.setVisibility(View.VISIBLE);

                        actionView.setOnClickListener(v -> {
                            startActivity(new Intent(this, NotificacoesActivity.class));
                            drawerLayout.closeDrawer(GravityCompat.START);
                        });
                    } else {
                        navigationView.getMenu()
                                .findItem(R.id.menu_notificacoes)
                                .setActionView(null);
                    }
                });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        drawerLayout.closeDrawer(GravityCompat.START);
        int id = item.getItemId();

        if (id == R.id.menu_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.menu_itens) {
            startActivity(new Intent(this, ItensActivity.class));
        } else if (id == R.id.menu_solicitacoes) {
            startActivity(new Intent(this, SolicitacoesActivity.class));
        } else if (id == R.id.menu_notificacoes) {
            startActivity(new Intent(this, NotificacoesActivity.class));
        } else if (id == R.id.menu_login) {
            startActivity(new Intent(this, LoginActivity.class));
        } else if (id == R.id.menu_signup) {
            startActivity(new Intent(this, CadastroActivity.class));
        } else if (id == R.id.menu_logout) {
            FirebaseAuth.getInstance().signOut();
            Toast.makeText(this, "Logout realizado", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
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