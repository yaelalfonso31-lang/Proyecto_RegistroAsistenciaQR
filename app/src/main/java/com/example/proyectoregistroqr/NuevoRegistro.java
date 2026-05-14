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

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import java.util.Calendar;
import java.util.Locale;
import java.text.SimpleDateFormat;

// Importaciones de Firebase
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class NuevoRegistro extends AppCompatActivity {

    private Button btnAtras, btnRegistrar;
    private EditText etMatricula, etNombre, etFecha, etHora;

    Calendar calendario = Calendar.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nuevo_registro);
        actualizarFechaEnCampo();
        actualizarHoraEnCampo();
        etFecha.setOnClickListener(v -> {
            new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendario.set(Calendar.YEAR, year);
                calendario.set(Calendar.MONTH, month);
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                actualizarFechaEnCampo();
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH)).show();
        });

        etHora.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendario.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendario.set(Calendar.MINUTE, minute);
                actualizarHoraEnCampo();
            }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), false).show();
        });

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

    private void actualizarFechaEnCampo() {
        String formato = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(formato, Locale.US);
        etFecha.setText(sdf.format(calendario.getTime()));
    }

    private void actualizarHoraEnCampo() {
        String formato = "hh:mm a";
        SimpleDateFormat sdf = new SimpleDateFormat(formato, Locale.US);
        etHora.setText(sdf.format(calendario.getTime()));
    }

    // 4. Mejorar la función de validación
    private void validarYGuardar() {
        String matricula = etMatricula.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();

        // Validación de Matrícula: Exactamente 9 números
        if (matricula.length() != 9) {
            etMatricula.setError("La matrícula debe tener exactamente 9 dígitos");
            etMatricula.requestFocus();
            return;
        }

        // Validación de Nombre: Solo letras y espacios (incluye acentos y ñ)
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            etNombre.setError("El nombre solo puede contener letras");
            etNombre.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("El campo nombre no puede estar vacío");
            return;
        }

        // Si todo es correcto, proceder a guardar en Firebase...
        Toast.makeText(this, "Validación exitosa", Toast.LENGTH_SHORT).show();
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