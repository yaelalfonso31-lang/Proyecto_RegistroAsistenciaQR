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

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingrese el nombre de la clase");
            return;
        }
        if (nrc.length() != 5) {
            etNrc.setError("El NRC debe tener exactamente 5 dígitos");
            return;
        }
        if (TextUtils.isEmpty(estudiantesStr)) {
            etEstudiantes.setError("Ingrese la cantidad de alumnos");
            return;
        }
        if (TextUtils.isEmpty(horario)) {
            etHorario.setError("Ingrese el horario");
            return;
        }

        int estudiantes = Integer.parseInt(estudiantesStr);

        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Clases");
        String idClase = myRef.push().getKey();

        Clase nuevaClase = new Clase(idClase, nombre, nrc, estudiantes, horario);

        if (idClase != null) {
            myRef.child(idClase).setValue(nuevaClase)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "Clase registrada exitosamente", Toast.LENGTH_SHORT).show();
                        finish();
                    })
                    .addOnFailureListener(e -> Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }
}