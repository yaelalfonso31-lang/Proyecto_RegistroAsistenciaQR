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

// Importaciones de Firebase
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NuevoRegistro extends AppCompatActivity {

    private Button btnAtras, btnRegistrar;
    private EditText etMatricula, etNombre, etFecha, etHora;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nuevo_registro);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 1. Enlazar variables con los IDs del diseño
        btnAtras = findViewById(R.id.btnAtras);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        etMatricula = findViewById(R.id.etMatricula);
        etNombre = findViewById(R.id.etNombre);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);

        btnRegistrar.setOnClickListener(v -> validarYGuardarEnFirebase());

        btnAtras.setOnClickListener(v -> {
            Toast.makeText(this, "Registro cancelado", Toast.LENGTH_SHORT).show();
            finish(); // Cierra esta pantalla y vuelve a la tabla
        });
    }

    private void validarYGuardarEnFirebase() {
        String matricula = etMatricula.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String hora = etHora.getText().toString().trim();

        // Validaciones básicas
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

        // 2. Conexión a Firebase
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Asistencias");

        // 3. Generar un ID único para este nuevo registro
        String nuevoId = myRef.push().getKey();

        // 4. Crear el objeto usando tu modelo
        TablaAsistencias nuevaAsistencia = new TablaAsistencias(nuevoId, matricula, nombre, fecha, hora);

        // 5. Enviar a la base de datos
        if (nuevoId != null) {
            myRef.child(nuevoId).setValue(nuevaAsistencia)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Registro guardado en Firebase", Toast.LENGTH_SHORT).show();
                        // Opcional: Redirigir a tu pantalla de éxito
                        Intent intent = new Intent(NuevoRegistro.this, Exito.class);
                        startActivity(intent);
                        finish(); // Cerrar esta pantalla
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
}