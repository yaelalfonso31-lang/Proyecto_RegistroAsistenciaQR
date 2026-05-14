package com.example.proyectoregistroqr;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class pantallaQR extends AppCompatActivity {

    private static final int CODIGO_PERMISO_CAMARA = 100;
    private DecoratedBarcodeView escanerEnVivo;
    private boolean yaEscaneado = false; // Bandera para evitar escaneos dobles rápidos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantalla_qr);

        escanerEnVivo = findViewById(R.id.escanerEnVivo);

        verificarPermisoCamara();
    }

    private void iniciarEscaner() {
        escanerEnVivo.decodeContinuous(new BarcodeCallback() {
            @Override
            public void barcodeResult(BarcodeResult result) {
                if (result.getText() != null && !yaEscaneado) {
                    yaEscaneado = true; // Bloqueamos para que no lea el mismo código 20 veces por segundo
                    escanerEnVivo.pause(); // Pausamos la cámara

                    String matriculaEscaneada = result.getText();

                    // Obtener fecha y hora actuales del teléfono
                    String fechaActual = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
                    String horaActual = new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date());

                    // Conectar a Firebase
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Asistencias");
                    String idGenerado = myRef.push().getKey();

                    // Creamos el objeto (Asumimos que el QR contiene la matrícula)
                    // Si tienes forma de saber el nombre, lo cambias; por ahora le ponemos "Alumno Escaneado"
                    TablaAsistencias nuevaAsistencia = new TablaAsistencias(
                            idGenerado,
                            matriculaEscaneada,
                            "Alumno Escaneado",
                            fechaActual,
                            horaActual
                    );

                    // Guardar en la base de datos
                    if (idGenerado != null) {
                        myRef.child(idGenerado).setValue(nuevaAsistencia)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(pantallaQR.this, "¡Asistencia registrada!", Toast.LENGTH_SHORT).show();

                                    // Ir a la pantalla de éxito con la animación
                                    Intent intent = new Intent(pantallaQR.this, Exito.class);
                                    startActivity(intent);
                                    finish(); // Cerramos la pantalla del escáner
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(pantallaQR.this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                    yaEscaneado = false; // Permitimos intentar de nuevo
                                    escanerEnVivo.resume();
                                });
                    }
                }
            }
        });

        escanerEnVivo.resume();
    }

    private void verificarPermisoCamara() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            iniciarEscaner();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CODIGO_PERMISO_CAMARA);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CODIGO_PERMISO_CAMARA) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                iniciarEscaner();
            } else {
                Toast.makeText(this, "Necesitas dar permiso para pasar lista", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        escanerEnVivo.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            yaEscaneado = false; // Reiniciamos la bandera si volvemos a la pantalla
            escanerEnVivo.resume();
        }
    }
}