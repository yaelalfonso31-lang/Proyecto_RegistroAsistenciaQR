package com.example.proyectoregistroqr;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

// Importaciones de la librería que acabamos de instalar
import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class pantallaQR extends AppCompatActivity {

    private static final int CODIGO_PERMISO_CAMARA = 100;
    private DecoratedBarcodeView escanerEnVivo;

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
                if (result.getText() != null) {
                    String matriculaOTexto = result.getText();

                    Toast.makeText(pantallaQR.this, "Asistencia de: " + matriculaOTexto, Toast.LENGTH_SHORT).show();

                    escanerEnVivo.pause();
                    escanerEnVivo.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            escanerEnVivo.resume();
                        }
                    }, 1500);
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
            escanerEnVivo.resume();
        }
    }
}