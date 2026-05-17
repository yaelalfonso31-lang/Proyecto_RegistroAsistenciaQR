package com.example.proyectoregistroqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class pantallaMenu extends AppCompatActivity {

    private Button btnEscaner, btnGenerar, btnGestion, btnInformes;
    private String rol, nrcClase, nombreClase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_menu);

        btnEscaner = findViewById(R.id.btnEscanerQR);
        btnGenerar = findViewById(R.id.btnGenerarQR);
        btnGestion = findViewById(R.id.btnGestion);
        btnInformes = findViewById(R.id.btnInformes);

        // 1. Recuperar el rol y los datos de la clase seleccionada
        rol = getIntent().getStringExtra("rol");
        nrcClase = getIntent().getStringExtra("nrc");
        nombreClase = getIntent().getStringExtra("nombreClase");

        // 2. Control de visibilidad por roles
        if (rol != null && rol.equals("admin")) {
            btnGenerar.setVisibility(View.VISIBLE);
            btnGestion.setVisibility(View.VISIBLE);
            btnInformes.setVisibility(View.VISIBLE);
            btnEscaner.setVisibility(View.GONE);
        } else {
            btnEscaner.setVisibility(View.VISIBLE);
            btnGenerar.setVisibility(View.GONE);
            btnGestion.setVisibility(View.GONE);
            btnInformes.setVisibility(View.GONE);
        }

        // 3. Configurar clics pasando el NRC de la clase actual de forma obligatoria
        btnGenerar.setOnClickListener(v -> {
            Intent intent = new Intent(this, GenerarQR.class);
            intent.putExtra("nrc", nrcClase);
            intent.putExtra("nombreClase", nombreClase);
            startActivity(intent);
        });

        btnGestion.setOnClickListener(v -> {
            Intent intent = new Intent(this, GestionActivity.class);
            intent.putExtra("nrc", nrcClase);
            intent.putExtra("nombreClase", nombreClase);
            startActivity(intent);
        });

        btnInformes.setOnClickListener(v -> {
            Intent intent = new Intent(this, Informes.class);
            intent.putExtra("nrc", nrcClase);
            intent.putExtra("nombreClase", nombreClase);
            startActivity(intent);
        });

        btnEscaner.setOnClickListener(v -> {
            startActivity(new Intent(this, pantallaQR.class));
        });


    }
}