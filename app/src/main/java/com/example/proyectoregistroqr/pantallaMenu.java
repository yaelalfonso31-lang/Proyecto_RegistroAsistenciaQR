package com.example.proyectoregistroqr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class pantallaMenu extends AppCompatActivity {

    private Button btnEscaner, btnGenerar, btnGestion, btnInformes, btnCerrarSesion;
    private String rol, nrcClase, nombreClase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_menu);

        btnEscaner = findViewById(R.id.btnEscanerQR);
        btnGenerar = findViewById(R.id.btnGenerarQR);
        btnGestion = findViewById(R.id.btnGestion);
        btnInformes = findViewById(R.id.btnInformes);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesionMenu); // El botón de hasta abajo

        // 1. Recuperar el rol y los datos de la clase seleccionada
        rol = getIntent().getStringExtra("rol");
        nrcClase = getIntent().getStringExtra("nrc");
        nombreClase = getIntent().getStringExtra("nombreClase");

        // 2. Control de visibilidad y lógica por roles
        if (rol != null && rol.equals("admin")) {
            // Vistas del maestro
            btnGenerar.setVisibility(View.VISIBLE);
            btnGestion.setVisibility(View.VISIBLE);
            btnInformes.setVisibility(View.VISIBLE);
            btnEscaner.setVisibility(View.GONE);

            // COMPORTAMIENTO PARA EL MAESTRO: Volver a clases
            btnCerrarSesion.setText("Volver a mis clases");
            // Le cambiamos el color rojo por un gris oscuro/neutro para que no parezca una alerta

            btnCerrarSesion.setOnClickListener(v -> {
                // Al usar finish(), simplemente cerramos este menú y se revela la pantalla
                // de Lista de Clases que ya estaba abierta justo debajo.
                finish();
            });

        } else {
            // Vistas del alumno
            btnEscaner.setVisibility(View.VISIBLE);
            btnGenerar.setVisibility(View.GONE);
            btnGestion.setVisibility(View.GONE);
            btnInformes.setVisibility(View.GONE);

            // COMPORTAMIENTO PARA EL ALUMNO: Cerrar sesión real
            btnCerrarSesion.setText("Cerrar Sesión");
            // Mantiene su color rojo de error que le pusiste en el XML

            btnCerrarSesion.setOnClickListener(v -> {
                // 1. Limpieza de SharedPreferences
                getSharedPreferences("SESION", MODE_PRIVATE).edit().clear().apply();

                // 2. Salida al Login limpiando el historial
                Intent intent = new Intent(pantallaMenu.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

                Toast.makeText(this, "Sesión finalizada", Toast.LENGTH_SHORT).show();
                finish();
            });
        }

        // 3. Configurar clics de navegación (pasando siempre el NRC contextual)
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