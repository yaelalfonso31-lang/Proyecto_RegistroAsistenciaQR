package com.example.proyectoregistroqr;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistroUsuario extends AppCompatActivity {

    private EditText etNombre, etCorreo, etPass, etCodigoMaestro;
    private RadioGroup rgRol;
    private Button btnRegistrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_usuario);

        etNombre = findViewById(R.id.etNombreRegistro);
        etCorreo = findViewById(R.id.etCorreoRegistro);
        etPass = findViewById(R.id.etPassRegistro);
        etCodigoMaestro = findViewById(R.id.etCodigoMaestro);
        rgRol = findViewById(R.id.rgRol);
        btnRegistrar = findViewById(R.id.btnFinalizarRegistro);

        // Mostrar/Ocultar código de maestro según selección
        rgRol.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbMaestro) {
                etCodigoMaestro.setVisibility(View.VISIBLE);
            } else {
                etCodigoMaestro.setVisibility(View.GONE);
            }
        });

        btnRegistrar.setOnClickListener(v -> registrarUsuario());
    }

    private void registrarUsuario() {
        String nombre = etNombre.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String pass = etPass.getText().toString().trim();
        String rol = ((RadioButton)findViewById(rgRol.getCheckedRadioButtonId())).getText().toString().toLowerCase();

        // Validación de código para maestros
        if (rol.equals("maestro")) {
            String codigoIngresado = etCodigoMaestro.getText().toString().trim();
            if (!codigoIngresado.equals("123456")) {
                etCodigoMaestro.setError("Código de maestro incorrecto");
                return;
            }
            rol = "admin"; // Guardamos internamente como admin
        }

        if (nombre.isEmpty() || correo.isEmpty() || pass.isEmpty()) {
            Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Guardar en Firebase bajo el nodo "Usuarios"
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference("Usuarios");
        String userId = mDatabase.push().getKey();
        Usuario nuevoUsuario = new Usuario(userId, nombre, correo, pass, rol);

        if (userId != null) {
            mDatabase.child(userId).setValue(nuevoUsuario).addOnSuccessListener(aVoid -> {
                Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show();
                finish();
            }).addOnFailureListener(e -> {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}