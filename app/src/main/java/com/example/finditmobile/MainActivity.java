package com.example.finditmobile;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Configura Toolbar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        // Configura logo redimensionado
        Drawable originalLogo = ContextCompat.getDrawable(this, R.drawable.findit_logo);
        Bitmap bitmap = ((BitmapDrawable) originalLogo).getBitmap();
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 84, 84, false);
        Drawable resizedDrawable = new BitmapDrawable(getResources(), resized);
        getSupportActionBar().setLogo(resizedDrawable);

        // Drawer
        drawerLayout = findViewById(R.id.drawer_layout);

        // Botão do menu na direita
        ImageButton menuButton = findViewById(R.id.menu_button);
        menuButton.setOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.END);
        });

        // Menu lateral
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(item -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            int id = item.getItemId();
            if (id == R.id.menu_home) {
                Toast.makeText(MainActivity.this, "Início selecionado", Toast.LENGTH_SHORT).show();
            } else if (id == R.id.menu_about) {
                Toast.makeText(MainActivity.this, "Sobre selecionado", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
    }
}