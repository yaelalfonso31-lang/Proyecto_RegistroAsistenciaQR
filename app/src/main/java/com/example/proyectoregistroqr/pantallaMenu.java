package com.example.proyectoregistroqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class pantallaMenu extends AppCompatActivity {
    Button btnEscaner, btnGenerar, btnGestion, btnInformes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_pantalla_menu);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


            btnEscaner = findViewById(R.id.btnEscanerQR);
            btnGenerar = findViewById(R.id.btnGenerarQR);
            btnGestion = findViewById(R.id.btnGestion);
            btnInformes = findViewById(R.id.btnInformes);

            // Recibir el rol del Intent
            String rol = getIntent().getStringExtra("rol");

            if (rol != null && rol.equals("admin")) {
                // Menú para el Maestro
                btnGenerar.setVisibility(View.VISIBLE);
                btnGestion.setVisibility(View.VISIBLE);
                btnInformes.setVisibility(View.VISIBLE);
                btnEscaner.setVisibility(View.GONE);
            } else {
                // Menú para el Alumno
                btnEscaner.setVisibility(View.VISIBLE);
                btnGenerar.setVisibility(View.GONE);
                btnGestion.setVisibility(View.GONE);
                btnInformes.setVisibility(View.GONE);
            }

            // Configurar clic para Generar QR
            btnGenerar.setOnClickListener(v -> {
                startActivity(new Intent(this, GenerarQR.class));
            });

        Button btnEscaner = findViewById(R.id.btnEscanerQR);

        btnEscaner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pantallaMenu.this, pantallaQR.class);
                startActivity(intent);
            }
        });

        Button btnGestion = findViewById(R.id.btnGestion);
        btnGestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pantallaMenu.this, GestionActivity.class);
                startActivity(intent);
            }
        });

        Button btnInformes = findViewById(R.id.btnInformes); // Asegúrate de tener este botón en tu layout del menú
        btnInformes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(pantallaMenu.this, Informes.class);
                startActivity(intent);
            }
        });

    }
}