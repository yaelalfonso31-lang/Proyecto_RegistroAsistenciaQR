package com.example.proyectoregistroqr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.google.zxing.BarcodeFormat;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GenerarQR extends AppCompatActivity {

    private ImageView ivCodigoQR;
    private TextView tvFecha;
    private Button btnVolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_qr);

        ivCodigoQR = findViewById(R.id.ivCodigoQR);
        tvFecha = findViewById(R.id.tvFechaQR);
        btnVolver = findViewById(R.id.btnVolverGenerar);

        // 1. Obtener la fecha actual del sistema
        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
        tvFecha.setText("Válido para: " + fechaHoy);

        // 2. Generar el QR con el texto de la fecha
        generarQR(fechaHoy);

        btnVolver.setOnClickListener(v -> finish());
    }

    private void generarQR(String texto) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            // Generamos el Bitmap (imagen) del QR con tamaño 400x400
            Bitmap bitmap = barcodeEncoder.encodeBitmap(texto, BarcodeFormat.QR_CODE, 400, 400);
            ivCodigoQR.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}