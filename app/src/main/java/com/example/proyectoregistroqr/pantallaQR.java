package com.example.proyectoregistroqr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class pantallaQR extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_qr);

        // IntentIntegrator maneja automáticamente los permisos de la cámara
        // y abre su propia interfaz de escaneo.
        new IntentIntegrator(this)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setPrompt("Escanea el código QR del Maestro")
                .setCameraId(0)
                .setBeepEnabled(true)
                .setOrientationLocked(false)
                .initiateScan();
    }

    // Este método se ejecuta automáticamente cuando el escáner lee algo o se cancela
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (result != null) {
            if (result.getContents() == null) {
                // Si el usuario presiona "Atrás" sin escanear
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Si escaneó exitosamente, el contenido del QR (la fecha) estará aquí
                String fechaQR = result.getContents();
                registrarAsistenciaFirebase(fechaQR);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void registrarAsistenciaFirebase(String fechaLeida) {
        // 1. Recuperar los datos del alumno de la "sesión" local (guardados en el Login)
        SharedPreferences prefs = getSharedPreferences("SESION", MODE_PRIVATE);
        String nombreAlumno = prefs.getString("nombre", "Alumno Desconocido");
        String matriculaAlumno = prefs.getString("matricula", "000000000");

        // 2. Obtener la hora actual exacta del sistema del teléfono del alumno
        String horaActual = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

        // 3. Conectar con el nodo "Asistencias" en Firebase
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Asistencias");
        String idAsistencia = myRef.push().getKey();

        // 4. Crear el objeto con tu modelo (TablaAsistencias)
        TablaAsistencias nuevaAsistencia = new TablaAsistencias(
                idAsistencia,
                matriculaAlumno,
                nombreAlumno,
                fechaLeida, // La fecha que proyectó el maestro en su QR
                horaActual
        );

        // 5. Guardar en la nube de Firebase
        if (idAsistencia != null) {
            myRef.child(idAsistencia).setValue(nuevaAsistencia)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(this, "¡Asistencia registrada!", Toast.LENGTH_SHORT).show();

                        // Si todo sale bien, vamos a la pantalla de Éxito para ver la animación
                        Intent intent = new Intent(pantallaQR.this, Exito.class);
                        startActivity(intent);
                        finish(); // Cerramos la pantalla actual
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error al registrar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        finish();
                    });
        }
    }
}