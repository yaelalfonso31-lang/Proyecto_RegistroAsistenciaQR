package com.example.proyectoregistroqr;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EditarRegistro extends AppCompatActivity {

    private Button btnCancelar, btnGuardar;
    private EditText etMatricula, etNombre, etFecha, etHora;
    private String idRegistro;
    private String nrcRegistro; // Variable vital para no perder la asistencia en la tabla
    private Calendar calendario = Calendar.getInstance();

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

        btnCancelar = findViewById(R.id.btnCancelar);
        btnGuardar = findViewById(R.id.btnGuardar);
        etMatricula = findViewById(R.id.etMatricula);
        etNombre = findViewById(R.id.etNombre);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);

        // Recuperamos los datos que nos envía la Gestión de Asistencias
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("matricula")) {
            idRegistro = intent.getStringExtra("id");
            nrcRegistro = intent.getStringExtra("nrc");
            etMatricula.setText(intent.getStringExtra("matricula"));
            etNombre.setText(intent.getStringExtra("nombre"));
            etFecha.setText(intent.getStringExtra("fecha"));
            etHora.setText(intent.getStringExtra("hora"));
        }

        etFecha.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendario.set(Calendar.YEAR, year);
                calendario.set(Calendar.MONTH, month);
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                actualizarFechaEnCampo();
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));

            // Barrera Visual: Deshabilita seleccionar fechas futuras
            dialog.getDatePicker().setMaxDate(System.currentTimeMillis());
            dialog.show();
        });

        etHora.setOnClickListener(v -> {
            new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                calendario.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendario.set(Calendar.MINUTE, minute);
                actualizarHoraEnCampo();
            }, calendario.get(Calendar.HOUR_OF_DAY), calendario.get(Calendar.MINUTE), false).show();
        });

        btnCancelar.setOnClickListener(v -> {
            Toast.makeText(this, "Edición cancelada", Toast.LENGTH_SHORT).show();
            finish();
        });

        btnGuardar.setOnClickListener(v -> validarYGuardar());
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

    private void validarYGuardar() {
        String matricula = etMatricula.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String hora = etHora.getText().toString().trim();

        // 1. Validaciones de Matrícula
        if (TextUtils.isEmpty(matricula)) {
            etMatricula.setError("Ingrese la matrícula");
            etMatricula.requestFocus();
            return;
        }
        if (matricula.length() != 9) {
            etMatricula.setError("La matrícula debe tener exactamente 9 dígitos");
            etMatricula.requestFocus();
            return;
        }

        // 2. Validaciones estrictas de Nombre
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingrese el nombre");
            etNombre.requestFocus();
            return;
        }
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            etNombre.setError("El nombre solo puede contener letras y espacios");
            etNombre.requestFocus();
            return;
        }

        // 3. Validaciones de campos de tiempo
        if (TextUtils.isEmpty(fecha)) {
            etFecha.setError("Ingrese la fecha");
            return;
        }
        if (TextUtils.isEmpty(hora)) {
            etHora.setError("Ingrese la hora");
            return;
        }

        // 4. Barrera Lógica: Bloquear fechas futuras si se intentan escribir o forzar
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date fechaIngresada = sdf.parse(fecha);
            Date fechaHoy = sdf.parse(sdf.format(new Date()));

            if (fechaIngresada != null && fechaIngresada.after(fechaHoy)) {
                etFecha.setError("La fecha no puede ser en el futuro");
                Toast.makeText(this, "No puedes registrar asistencias de días que aún no pasan", Toast.LENGTH_LONG).show();
                return;
            }
        } catch (Exception e) {
            etFecha.setError("Formato de fecha inválido");
            return;
        }

        actualizarEnFirebase(matricula, nombre, fecha, hora);
    }

    private void actualizarEnFirebase(String matricula, String nombre, String fecha, String hora) {
        if (idRegistro == null || idRegistro.isEmpty()) {
            Toast.makeText(this, "Error: No se encontró el ID del registro original", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Asistencias").child(idRegistro);

        TablaAsistencias asistenciaActualizada = new TablaAsistencias(idRegistro, matricula, nombre, fecha, hora);

        // Inyectamos el NRC para no perder la coherencia de los datos en Firebase
        if (nrcRegistro != null) {
            asistenciaActualizada.setNrc(nrcRegistro);
        }

        myRef.setValue(asistenciaActualizada).addOnSuccessListener(aVoid -> {
            Toast.makeText(this, "Asistencia actualizada correctamente", Toast.LENGTH_SHORT).show();
            finish();
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error de red: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}