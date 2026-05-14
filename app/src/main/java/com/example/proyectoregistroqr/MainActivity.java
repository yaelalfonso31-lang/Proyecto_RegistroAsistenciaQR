package com.example.proyectoregistroqr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private EditText etCorreo, etPass;
    private Button btnLogin;
    private TextView tvRegistro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etCorreo = findViewById(R.id.etCorreoLogin);
        etPass = findViewById(R.id.etPassLogin);
        btnLogin = findViewById(R.id.btningresar);
        tvRegistro = findViewById(R.id.tvIrRegistro);

        // Redirección a la pantalla de registro
        tvRegistro.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistroUsuario.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> validarUsuario());
    }

    private void validarUsuario() {
        String correoIngresado = etCorreo.getText().toString().trim();
        String passIngresada = etPass.getText().toString().trim();

        if (correoIngresado.isEmpty() || passIngresada.isEmpty()) {
            Toast.makeText(this, "Ingresa tus credenciales", Toast.LENGTH_SHORT).show();
            return;
        }

        // Referencia a la tabla de Usuarios
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Usuarios");

        // Buscamos al usuario por su correo
        Query checkUser = reference.orderByChild("correo").equalTo(correoIngresado);

        checkUser.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    // El usuario existe, ahora validamos la contraseña
                    for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                        String passDB = userSnapshot.child("contrasena").getValue(String.class);

                        if (passDB != null && passDB.equals(passIngresada)) {
                            // Contraseña correcta, leemos el rol
                            String rol = userSnapshot.child("rol").getValue(String.class);
                            String nombreDB = userSnapshot.child("nombre").getValue(String.class);
                            String matriculaDB = userSnapshot.child("matricula").getValue(String.class);
                            getSharedPreferences("SESION", MODE_PRIVATE).edit()
                                    .putString("nombre", nombreDB)
                                    .putString("matricula", matriculaDB)
                                    .apply();

                            accederSegunRol(rol);
                        } else {
                            Toast.makeText(MainActivity.this, "Contraseña incorrecta", Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(MainActivity.this, "El usuario no existe", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(MainActivity.this, "Error de conexión", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void accederSegunRol(String rol) {
        Intent intent = new Intent(MainActivity.this, pantallaMenu.class);
        intent.putExtra("rol", rol); // Enviamos el rol ("admin" o "alumno")
        startActivity(intent);
        finish();
    }
}