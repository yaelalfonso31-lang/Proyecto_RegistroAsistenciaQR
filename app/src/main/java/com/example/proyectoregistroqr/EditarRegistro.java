package com.example.proyectoregistroqr;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

// Importaciones necesarias para Firebase
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditarRegistro extends AppCompatActivity {

    private Button btnCancelar, btnGuardar;
    private EditText etMatricula, etNombre, etFecha, etHora;
    private String idRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_editar_registro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Enlazamos las variables con los IDs del diseño
        btnCancelar = findViewById(R.id.btnCancelar);
        btnGuardar = findViewById(R.id.btnGuardar);
        etMatricula = findViewById(R.id.etMatricula);
        etNombre = findViewById(R.id.etNombre);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);

        // 2. Recuperamos los datos que nos envía la Gestión de Asistencias
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("matricula")) {
            idRegistro = intent.getStringExtra("id");
            etMatricula.setText(intent.getStringExtra("matricula"));
            etNombre.setText(intent.getStringExtra("nombre"));
            etFecha.setText(intent.getStringExtra("fecha"));
            etHora.setText(intent.getStringExtra("hora"));
        }

        // 3. Configuración de clics
        btnCancelar.setOnClickListener(v -> {
            Toast.makeText(this, "Edición cancelada", Toast.LENGTH_SHORT).show();
            finish(); // Cierra esta pantalla
        });

        btnGuardar.setOnClickListener(v -> validarYGuardar());
    }

    private void validarYGuardar() {
        String matricula = etMatricula.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String hora = etHora.getText().toString().trim();

        // Validaciones para que no dejen campos en blanco
        if (TextUtils.isEmpty(matricula)) {
            etMatricula.setError("Ingrese la matrícula");
            etMatricula.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingrese el nombre");
            etNombre.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(fecha)) {
            etFecha.setError("Ingrese la fecha");
            etFecha.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(hora)) {
            etHora.setError("Ingrese la hora");
            etHora.requestFocus();
            return;
        }

        // Si pasa todas las validaciones, actualizamos en Firebase
        actualizarEnFirebase(matricula, nombre, fecha, hora);
    }

    private void actualizarEnFirebase(String matricula, String nombre, String fecha, String hora) {
        // Verificamos que tengamos un ID válido para no crear un registro nuevo por accidente
        if (idRegistro == null || idRegistro.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró el ID del registro", Toast.LENGTH_SHORT).show();
            return;
        }

        // Apuntamos al nodo específico de esta asistencia usando su ID único
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Asistencias").child(idRegistro);

        // Creamos el objeto con los datos modificados
        TablaAsistencias asistenciaActualizada = new TablaAsistencias(idRegistro, matricula, nombre, fecha, hora);

        // Sobrescribimos la información en la base de datos
        myRef.setValue(asistenciaActualizada).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Registro actualizado en Firebase exitosamente", Toast.LENGTH_SHORT).show();
            finish(); // Cerramos la vista para volver a la tabla de gestión
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error de conexión: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}