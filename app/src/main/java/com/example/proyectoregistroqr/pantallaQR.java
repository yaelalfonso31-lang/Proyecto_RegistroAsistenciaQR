package com.example.proyectoregistroqr;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class pantallaQR extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle Bundle) {
        super.onCreate(Bundle);
        setContentView(R.layout.activity_pantalla_qr);

        // Iniciar el escáner inmediatamente al abrir la pantalla
        new IntentIntegrator(this)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
                .setPrompt("Escanea el código QR del Maestro")
                .setCameraId(0)
                .setBeepEnabled(true)
                .setOrientationLocked(true) // Mantenemos en true para bloquear la rotación
                .setCaptureActivity(CapturaVertical.class) // Llamamos a la pantalla vertical
                .initiateScan();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Escaneo cancelado", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // El contenido leído contiene "NRC;FECHA"
                String datosQR = result.getContents();
                procesarAsistenciaAutomatica(datosQR);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void procesarAsistenciaAutomatica(String datosQR) {
        if (datosQR != null && datosQR.contains(";")) {
            String[] partes = datosQR.split(";");
            String nrcLeido = partes[0];
            String fechaLeida = partes[1];

            SharedPreferences prefs = getSharedPreferences("SESION", MODE_PRIVATE);
            String nombreAlumno = prefs.getString("nombre", "Alumno Desconocido");
            String matriculaAlumno = prefs.getString("matricula", "000000000");

            String horaActual = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());
            DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Asistencias");

            // 1. CREAMOS EL ID ÚNICO COMPUESTO
            String idAsistenciaUnico = matriculaAlumno + "_" + nrcLeido + "_" + fechaLeida;

            // 2. VERIFICAMOS SI EL ALUMNO YA ESCANEÓ HOY PARA ESTA CLASE
            myRef.child(idAsistenciaUnico).addListenerForSingleValueEvent(new com.google.firebase.database.ValueEventListener() {
                @Override
                public void onDataChange(@NonNull com.google.firebase.database.DataSnapshot snapshot) {
                    if (snapshot.exists()) {
                        // El alumno intentó escanear por segunda vez el mismo día
                        Toast.makeText(pantallaQR.this, "Ya tienes asistencia registrada para esta clase hoy", Toast.LENGTH_LONG).show();
                        finish(); // Lo regresamos a su menú sin registrar nada
                    } else {
                        // Es su primer escaneo del día para esta clase, lo registramos
                        TablaAsistencias nuevaAsistencia = new TablaAsistencias(
                                idAsistenciaUnico,
                                matriculaAlumno,
                                nombreAlumno,
                                fechaLeida,
                                horaActual
                        );
                        nuevaAsistencia.setNrc(nrcLeido);

                        myRef.child(idAsistenciaUnico).setValue(nuevaAsistencia)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(pantallaQR.this, "¡Asistencia registrada con éxito!", Toast.LENGTH_LONG).show();
                                    startActivity(new Intent(pantallaQR.this, Exito.class));
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(pantallaQR.this, "Error de conexión", Toast.LENGTH_SHORT).show();
                                    finish();
                                });
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(pantallaQR.this, "Error al verificar asistencia", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        } else {
            Toast.makeText(this, "Código QR inválido o no pertenece a una clase", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}