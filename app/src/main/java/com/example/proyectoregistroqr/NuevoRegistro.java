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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class NuevoRegistro extends AppCompatActivity {

    private Button btnAtras, btnRegistrar;
    private EditText etMatricula, etNombre, etFecha, etHora;
    private Calendar calendario = Calendar.getInstance();

    private String nrcClaseSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_nuevo_registro);

        nrcClaseSeleccionada = getIntent().getStringExtra("nrc");
        if (nrcClaseSeleccionada == null) nrcClaseSeleccionada = "00000"; // Por seguridad

        // 1. PRIMERO enlazamos todas las variables con los IDs del diseño
        btnAtras = findViewById(R.id.btnAtras);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        etMatricula = findViewById(R.id.etMatricula);
        etNombre = findViewById(R.id.etNombre);
        etFecha = findViewById(R.id.etFecha);
        etHora = findViewById(R.id.etHora);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // 2. DESPUÉS usamos las vistas para poner la fecha/hora de hoy por defecto
        actualizarFechaEnCampo();
        actualizarHoraEnCampo();

        // 3. Configuramos los selectores (Calendario y Reloj)
        etFecha.setOnClickListener(v -> {
            DatePickerDialog dialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
                calendario.set(Calendar.YEAR, year);
                calendario.set(Calendar.MONTH, month);
                calendario.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                actualizarFechaEnCampo();
            }, calendario.get(Calendar.YEAR), calendario.get(Calendar.MONTH), calendario.get(Calendar.DAY_OF_MONTH));

            // Bloqueo Visual: Deshabilita seleccionar fechas en el futuro
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

        // 4. Configuramos los botones
        btnRegistrar.setOnClickListener(v -> validarYGuardarEnFirebase());

        btnAtras.setOnClickListener(v -> {
            Toast.makeText(this, "Registro cancelado", Toast.LENGTH_SHORT).show();
            finish();
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

    private void validarYGuardarEnFirebase() {
        String matricula = etMatricula.getText().toString().trim();
        String nombre = etNombre.getText().toString().trim();
        String fecha = etFecha.getText().toString().trim();
        String hora = etHora.getText().toString().trim();

        // Validaciones estrictas de Matrícula y Nombre
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
        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingrese el nombre");
            etNombre.requestFocus();
            return;
        }
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            etNombre.setError("El nombre solo puede contener letras");
            etNombre.requestFocus();
            return;
        }

        // Validación de campos de tiempo
        if (TextUtils.isEmpty(fecha)) {
            etFecha.setError("Ingrese la fecha");
            return;
        }
        if (TextUtils.isEmpty(hora)) {
            etHora.setError("Ingrese la hora");
            return;
        }

        // Validación Lógica: Bloquear fechas futuras por código
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date fechaIngresada = sdf.parse(fecha);

            // Obtenemos la fecha de hoy "limpia" (sin horas) para comparar justamente
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

        // Conexión a Firebase
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Asistencias");
        String idRegistroUnico = matricula + "_" + nrcClaseSeleccionada + "_" + fecha;

        // BUSCAMOS SI YA EXISTE EN FIREBASE
        myRef.child(idRegistroUnico).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // Si ya existe, detenemos el proceso y avisamos
                    Toast.makeText(NuevoRegistro.this, "El alumno ya tiene asistencia registrada en esta clase hoy", Toast.LENGTH_LONG).show();
                } else {
                    // Si no existe, creamos la asistencia y le INYECTAMOS EL NRC para que aparezca en la tabla
                    TablaAsistencias nuevaAsistencia = new TablaAsistencias(idRegistroUnico, matricula, nombre, fecha, hora);
                    nuevaAsistencia.setNrc(nrcClaseSeleccionada);

                    myRef.child(idRegistroUnico).setValue(nuevaAsistencia)
                            .addOnSuccessListener(aVoid -> {
                                Toast.makeText(NuevoRegistro.this, "Registro guardado exitosamente", Toast.LENGTH_SHORT).show();
                                finish(); // Cerramos y volvemos a la tabla (donde ahora sí aparecerá)
                            })
                            .addOnFailureListener(e -> Toast.makeText(NuevoRegistro.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(NuevoRegistro.this, "Error de red", Toast.LENGTH_SHORT).show();
            }
        });
    }
}