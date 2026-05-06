package com.example.proyectoregistroqr;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NuevoRegistro extends AppCompatActivity {

    Button btnAtras, btnRegistrar;
    EditText etNombre, etApellidos, etTelefono, etCorreo, etEdad;

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

        btnAtras = findViewById(R.id.btnAtras);
        btnRegistrar = findViewById(R.id.btnRegistrar);
        etNombre = findViewById(R.id.etNombre);
        etApellidos = findViewById(R.id.etApellidos);
        etTelefono = findViewById(R.id.etTelefono);
        etCorreo = findViewById(R.id.etCorreo);
        etEdad = findViewById(R.id.etEdad);

        btnRegistrar.setOnClickListener(v -> validarFormulario());

        btnAtras.setOnClickListener(v -> {
            Toast.makeText(this, "Registro cancelado", Toast.LENGTH_SHORT).show();
        });
    }

    private void validarFormulario() {
        String nombre = etNombre.getText().toString().trim();
        String apellidos = etApellidos.getText().toString().trim();
        String telefono = etTelefono.getText().toString().trim();
        String correo = etCorreo.getText().toString().trim();
        String edadStr = etEdad.getText().toString().trim();

        if (TextUtils.isEmpty(nombre)) {
            etNombre.setError("Ingrese el nombre");
            etNombre.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(apellidos)) {
            etApellidos.setError("Ingrese los apellidos");
            etApellidos.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(telefono)) {
            etTelefono.setError("Ingrese el teléfono");
            etTelefono.requestFocus();
            return;
        }

        if (!telefono.matches("\\d{10}")) {
            etTelefono.setError("Teléfono inválido (10 dígitos)");
            etTelefono.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(correo)) {
            etCorreo.setError("Ingrese el correo");
            etCorreo.requestFocus();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(correo).matches()) {
            etCorreo.setError("Correo inválido");
            etCorreo.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(edadStr)) {
            etEdad.setError("Ingrese la edad");
            etEdad.requestFocus();
            return;
        }

        int edad;
        try {
            edad = Integer.parseInt(edadStr);
        } catch (NumberFormatException e) {
            etEdad.setError("Edad inválida");
            etEdad.requestFocus();
            return;
        }

        if (edad < 1 || edad > 120) {
            etEdad.setError("Edad fuera de rango");
            etEdad.requestFocus();
            return;
        }

        Toast.makeText(this, "Registro guardado correctamente", Toast.LENGTH_SHORT).show();

        etNombre.setText("");
        etApellidos.setText("");
        etTelefono.setText("");
        etCorreo.setText("");
        etEdad.setText("");
    }
}