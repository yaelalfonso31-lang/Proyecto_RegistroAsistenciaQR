package com.example.proyectoregistroqr;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;

public class Informes extends AppCompatActivity {

    private PieChart graficaPastel;
    private Button btnAtras;
    private TextView txtAsistencia, txtFaltas;

    // Referencias de Firebase Realtime Database
    private DatabaseReference asistenciasRef;
    private DatabaseReference clasesRef;

    private int totalAlumnosInscritos = 0;
    private String nrcClaseActual;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informes);

        // 1. Recuperamos el NRC de la clase que estamos gestionando
        nrcClaseActual = getIntent().getStringExtra("nrc");

        // 2. Enlazamos las vistas
        graficaPastel = findViewById(R.id.graficaPastel);
        txtAsistencia = findViewById(R.id.txtAsistencia);
        txtFaltas = findViewById(R.id.txtFaltas);
        btnAtras = findViewById(R.id.btnAtras);

        configurarGrafica();

        // 3. Inicializamos los nodos correspondientes de Firebase
        asistenciasRef = FirebaseDatabase.getInstance().getReference("Asistencias");
        clasesRef = FirebaseDatabase.getInstance().getReference("Clases");

        // 4. Comenzamos el proceso de cálculo
        obtenerTotalAlumnosYCalcular();

        // 5. Botón de retroceso (finish para no perder el menú principal)
        btnAtras.setOnClickListener(v -> finish());
    }

    private void configurarGrafica() {
        graficaPastel.setUsePercentValues(true);
        graficaPastel.getDescription().setEnabled(false);
        graficaPastel.setExtraOffsets(5, 10, 5, 5);
        graficaPastel.setDragDecelerationFrictionCoef(0.95f);

        graficaPastel.setDrawHoleEnabled(true);
        graficaPastel.setHoleColor(Color.WHITE);
        graficaPastel.setTransparentCircleRadius(61f);
        graficaPastel.setCenterText("Asistencias\nTotales");
        graficaPastel.setCenterTextSize(16f);

        Legend leyenda = graficaPastel.getLegend();
        leyenda.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        leyenda.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        leyenda.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        leyenda.setDrawInside(false);
        leyenda.setTextSize(14f);
    }

    private void obtenerTotalAlumnosYCalcular() {
        if (nrcClaseActual == null) return;

        // Buscamos la clase específica por su NRC para saber cuántos alumnos tiene inscritos
        clasesRef.orderByChild("nrc").equalTo(nrcClaseActual).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                totalAlumnosInscritos = 0; // Valor por defecto
                if (snapshot.exists()) {
                    for (DataSnapshot claseSnapshot : snapshot.getChildren()) {
                        Integer inscritos = claseSnapshot.child("estudiantesInscritos").getValue(Integer.class);
                        if (inscritos != null) {
                            totalAlumnosInscritos = inscritos;
                        }
                    }
                }
                // Una vez que sabemos cuántos alumnos hay en ESTA clase, calculamos asistencias
                calcularEstadisticasReales();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Informes.this, "Error al cargar datos de la clase", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calcularEstadisticasReales() {
        // Escuchamos los registros de asistencias para recalcular si hay cambios en vivo
        asistenciasRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int totalAsistencias = 0;
                HashSet<String> diasUnicos = new HashSet<>();

                // Recorremos todos los escaneos registrados
                for (DataSnapshot asistenciaSnapshot : snapshot.getChildren()) {
                    String nrcAsistencia = asistenciaSnapshot.child("nrc").getValue(String.class);

                    // Solo contamos las asistencias que pertenecen a ESTA clase (NRC)
                    if (nrcAsistencia != null && nrcAsistencia.equals(nrcClaseActual)) {
                        totalAsistencias++;
                        String fecha = asistenciaSnapshot.child("fecha").getValue(String.class);
                        if (fecha != null) {
                            diasUnicos.add(fecha);
                        }
                    }
                }

                int clasesTotales = diasUnicos.size();
                if (clasesTotales == 0) clasesTotales = 1; // Prevenir división entre cero
                if (totalAlumnosInscritos == 0) totalAlumnosInscritos = 1; // Prevenir división entre cero

                // El universo total esperado: alumnos inscritos multiplicados por los días que se ha pasado lista
                int totalEsperado = totalAlumnosInscritos * clasesTotales;

                // Las faltas son las asistencias que se esperaban pero nunca se registraron
                int totalFaltas = totalEsperado - totalAsistencias;
                if (totalFaltas < 0) totalFaltas = 0;

                // Calculamos los porcentajes
                float porcAsistencias = ((float) totalAsistencias / totalEsperado) * 100;
                float porcFaltas = 100 - porcAsistencias;

                // Actualizamos la interfaz gráfica
                actualizarInterfaz(porcAsistencias, porcFaltas);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(Informes.this, "Error al recuperar asistencias", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void actualizarInterfaz(float porcAsistencias, float porcFaltas) {
        // 1. Modificar los TextViews con un decimal
        txtAsistencia.setText(String.format(Locale.US, "%.1f%%", porcAsistencias));
        txtFaltas.setText(String.format(Locale.US, "%.1f%%", porcFaltas));

        // 2. Preparar los datos para la Gráfica de Pastel
        ArrayList<PieEntry> entradas = new ArrayList<>();
        entradas.add(new PieEntry(porcAsistencias, "Asistencias"));
        entradas.add(new PieEntry(porcFaltas, "Faltas/Retardos"));

        PieDataSet dataSet = new PieDataSet(entradas, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Colores de la gráfica
        ArrayList<Integer> colores = new ArrayList<>();
        colores.add(Color.rgb(76, 175, 80)); // Verde
        colores.add(Color.rgb(244, 67, 54)); // Rojo
        dataSet.setColors(colores);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(graficaPastel));
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        // 3. Renderizar
        graficaPastel.setData(data);
        graficaPastel.invalidate();
    }
}