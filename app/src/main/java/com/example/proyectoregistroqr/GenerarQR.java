package com.example.proyectoregistroqr;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
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
    private TextView tvFecha, tvClaseInfo;
    private ImageButton btnVolver;
    private String nrcClase, nombreClase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_qr);

        ivCodigoQR = findViewById(R.id.ivCodigoQR);
        tvFecha = findViewById(R.id.tvFechaQR);
        tvClaseInfo = findViewById(R.id.tvClaseInfo); // Asegúrate de tener o agregar este TextView en tu XML si quieres mostrar el nombre
        btnVolver = findViewById(R.id.btnVolverGenerar);

        // 1. Obtener datos de la asignatura actual
        nrcClase = getIntent().getStringExtra("nrc");
        nombreClase = getIntent().getStringExtra("nombreClase");

        String fechaHoy = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (tvClaseInfo != null && nombreClase != null) {
            tvClaseInfo.setText(nombreClase + " (NRC: " + nrcClase + ")");
        }
        tvFecha.setText("Válido para el día: " + fechaHoy);

        // 2. Formatear contenido combinando NRC y Fecha. Esto garantiza unicidad por día y clase.
        String contenidoQR = nrcClase + ";" + fechaHoy;
        generarQR(contenidoQR);

        btnVolver.setOnClickListener(v -> finish());
    }

    private void generarQR(String texto) {
        try {
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.encodeBitmap(texto, BarcodeFormat.QR_CODE, 400, 400);
            ivCodigoQR.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}