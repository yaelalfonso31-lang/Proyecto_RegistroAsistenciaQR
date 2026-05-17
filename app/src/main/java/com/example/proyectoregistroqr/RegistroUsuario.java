package com.example.proyectoregistroqr;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistroUsuario extends AppCompatActivity {

    private EditText etNombre, etCorreo, etPass, etCodigoMaestro, etMatricula;
    private RadioGroup rgRol;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario);

        etNombre = findViewById(R.id.etNombreRegistro);
        etCorreo = findViewById(R.id.etCorreoRegistro);
        etPass = findViewById(R.id.etPassRegistro);
        etMatricula = findViewById(R.id.etMatriculaRegistro);
        etCodigoMaestro = findViewById(R.id.etCodigoMaestro);
        rgRol = findViewById(R.id.rgRol);
        btnRegistrar = findViewById(R.id.btnFinalizarRegistro);

        // Mostrar/Ocultar código de maestro según selección
        rgRol.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMaestro) {
                etCodigoMaestro.setVisibility(View.VISIBLE);
                etMatricula.setVisibility(View.GONE); // El maestro no requiere matrícula escolar
            } else {
                etCodigoMaestro.setVisibility(View.GONE);
                etMatricula.setVisibility(View.VISIBLE); // El alumno requiere matrícula obligatoriamente
            }
        });

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        String rolSeleccionado = ((RadioButton)findViewById(rgRol.getCheckedRadioButtonId())).getText().toString().toLowerCase();

        // Usamos un array de un solo elemento para poder modificar la variable dentro del Listener de Firebase
        final String[] rol = {rolSeleccionado};
        final String[] matricula = {"N/A"};

        // 1. Validaciones generales (Locales)
        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(correo) || TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Completa todos los campos principales", Toast.LENGTH_SHORT).show();
            return;
        }

        // 2. Validaciones específicas según reglas de negocio por rol
        if (rol[0].equals("maestro")) {
            String codigoIngresado = etCodigoMaestro.getText().toString().trim();
            if (!codigoIngresado.equals("123456")) {
                etCodigoMaestro.setError("Código de maestro incorrecto");
                etCodigoMaestro.requestFocus();
                return;
            }
            rol[0] = "admin"; // Clasificación interna
        } else {
            matricula[0] = etMatricula.getText().toString().trim();
            if (TextUtils.isEmpty(matricula[0])) {
                etMatricula.setError("La matrícula es obligatoria para los alumnos");
                etMatricula.requestFocus();
                return;
            }
            if (matricula[0].length() != 9) {
                etMatricula.setError("La matrícula debe tener exactamente 9 dígitos");
                etMatricula.requestFocus();
                return;
            }
        }

        // 3. Conexión a Firebase para validación de DUPLICADOS
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Usuarios");

        // Leemos todos los usuarios una sola vez para buscar coincidencias
        mDatabase.addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
            @Override
            public void onDataChange(com.google.firebase.database.DataSnapshot snapshot) {
                boolean existeDuplicado = false;

                for (com.google.firebase.database.DataSnapshot userSnapshot : snapshot.getChildren()) {
                    String correoDB = userSnapshot.child("correo").getValue(String.class);
                    String matriculaDB = userSnapshot.child("matricula").getValue(String.class);

                    // A. Verificamos si el correo ya existe (Aplica para maestros y alumnos)
                    if (correoDB != null && correoDB.equalsIgnoreCase(correo)) {
                        etCorreo.setError("Este correo ya está registrado");
                        etCorreo.requestFocus();
                        existeDuplicado = true;
                        break;
                    }

                    // B. Verificamos si la matrícula ya existe (Solo aplica para alumnos)
                    if (rol[0].equals("alumno") && matriculaDB != null && matriculaDB.equals(matricula[0])) {
                        etMatricula.setError("Esta matrícula ya pertenece a otra cuenta");
                        etMatricula.requestFocus();
                        existeDuplicado = true;
                        break;
                    }
                }

                // 4. Si no hay duplicados, procedemos a guardar el usuario en la nube
                if (!existeDuplicado) {
                    String userId = mDatabase.push().getKey();
                    Usuario nuevoUsuario = new Usuario(userId, nombre, correo, pass, rol[0], matricula[0]);

                    if (userId != null) {
                        mDatabase.child(userId).setValue(nuevoUsuario).addOnSuccessListener(aVoid -> {
                            Toast.makeText(RegistroUsuario.this, "Cuenta registrada exitosamente", Toast.LENGTH_SHORT).show();
                            finish(); // Retorno a la pantalla de autenticación
                        }).addOnFailureListener(e -> {
                            Toast.makeText(RegistroUsuario.this, "Error al guardar en la nube: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(RegistroUsuario.this, "Error de conexión al verificar datos", Toast.LENGTH_SHORT).show();
            }
        });
    }
}