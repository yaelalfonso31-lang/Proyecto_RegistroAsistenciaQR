package com.example.proyectoregistroqr;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CrearClase extends AppCompatActivity {

    private EditText etNombre, etNrc, etEstudiantes, etHorario;
    private Button btnCancelar, btnGuardar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crear_clase);

        etNombre = findViewById(R.id.etNombreClase);
        etNrc = findViewById(R.id.etNrcClase);
        etEstudiantes = findViewById(R.id.etEstudiantesClase);
        etHorario = findViewById(R.id.etHorarioClase);
        btnCancelar = findViewById(R.id.btnCancelarClase);
        btnGuardar = findViewById(R.id.btnGuardarClase);

        btnCancelar.setOnClickListener(v -> finish());
        btnGuardar.setOnClickListener(v -> guardarClaseFirebase());
    }

    private void guardarClaseFirebase() {
        String nombre = etNombre.getText().toString().trim();
        String nrc = etNrc.getText().toString().trim();
        String estudiantesStr = etEstudiantes.getText().toString().trim();
        String horario = etHorario.getText().toString().trim();

        // 1. Verificar que no haya campos completamente vacíos
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(nrc) || TextUtils.isEmpty(estudiantesStr) || TextUtils.isEmpty(horario)) {
            Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validar Nombre: Solo letras y espacios permitidos
        if (!nombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
            etNombre.setError("El nombre de la materia solo puede contener letras y espacios");
            etNombre.requestFocus();
            return;
        }

        // 3. Validar NRC: Exactamente 5 dígitos
        if (nrc.length() != 5) {
            etNrc.setError("El NRC debe tener exactamente 5 dígitos");
            etNrc.requestFocus();
            return;
        }

        // 4. Validar cantidad de Estudiantes (Máximo 100, Mínimo 1)
        int estudiantes = 0;
        try {
            estudiantes = Integer.parseInt(estudiantesStr);
            if (estudiantes <= 0) {
                etEstudiantes.setError("Debe haber al menos 1 estudiante inscrito");
                etEstudiantes.requestFocus();
                return;
            }
            if (estudiantes > 100) {
                etEstudiantes.setError("El límite máximo permitido es de 100 alumnos");
                etEstudiantes.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etEstudiantes.setError("Por favor, ingresa un número válido");
            etEstudiantes.requestFocus();
            return;
        }

        // 5. Conexión y guardado en Firebase
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Clases");
        String idClase = myRef.push().getKey();

        Clase nuevaClase = new Clase(idClase, nombre, nrc, estudiantes, horario);

        if (idClase != null) {
            myRef.child(idClase).setValue(nuevaClase)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Clase registrada exitosamente", Toast.LENGTH_SHORT).show();
                        finish(); // Se cierra y vuelve a la lista
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error de red: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}