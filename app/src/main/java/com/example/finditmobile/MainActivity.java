package com.example.finditmobile;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Conecta e define a Toolbar como ActionBar
        Toolbar toolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        Drawable originalLogo = ContextCompat.getDrawable(this, R.drawable.findit_logo);
        Bitmap bitmap = ((BitmapDrawable) originalLogo).getBitmap();
        Bitmap resized = Bitmap.createScaledBitmap(bitmap, 84, 84, false);
        Drawable resizedDrawable = new BitmapDrawable(getResources(), resized);

        getSupportActionBar().setLogo(resizedDrawable);

    }
}