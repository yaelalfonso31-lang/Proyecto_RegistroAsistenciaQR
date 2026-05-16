package com.example.proyectoregistroqr;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.util.ArrayList;

public class Informes extends AppCompatActivity {

    private PieChart graficaPastel;
    private Button btnAtras;
    private TextView txtAsistencia, txtFaltas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_informes);

        graficaPastel = findViewById(R.id.graficaPastel);
        configurarGrafica();
        llenarDatosGrafica();
        txtAsistencia = findViewById(R.id.txtAsistencia);
        txtFaltas = findViewById(R.id.txtFaltas);

        btnAtras = findViewById(R.id.btnAtras);

        btnAtras.setOnClickListener(v -> {
            Intent intent = new Intent(Informes.this, pantallaMenu.class);
            startActivity(intent);
        });
    }

    private void configurarGrafica() {
        graficaPastel.setUsePercentValues(true);
        graficaPastel.getDescription().setEnabled(false);
        graficaPastel.setExtraOffsets(5, 10, 5, 5);
        graficaPastel.setDragDecelerationFrictionCoef(0.95f);

        // Agujero en el centro (estilo dona)
        graficaPastel.setDrawHoleEnabled(true);
        graficaPastel.setHoleColor(Color.WHITE);
        graficaPastel.setTransparentCircleRadius(61f);
        graficaPastel.setCenterText("Asistencias\nTotales");
        graficaPastel.setCenterTextSize(16f);

        // Leyendas (los cuadritos de colores abajo)
        Legend leyenda = graficaPastel.getLegend();
        leyenda.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        leyenda.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        leyenda.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        leyenda.setDrawInside(false);
        leyenda.setTextSize(14f);
    }

    private void llenarDatosGrafica() {
        // 1. Crear las entradas (Aquí van tus datos simulados)
        ArrayList<PieEntry> entradas = new ArrayList<>();
        entradas.add(new PieEntry(85f, "Asistencias")); // 85% asistieron
        entradas.add(new PieEntry(15f, "Faltas/Retardos")); // 15% faltaron

        // 2. Crear el set de datos y darle estilo
        PieDataSet dataSet = new PieDataSet(entradas, "");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // 3. Asignar colores (Puedes usar los predeterminados de la librería)
        ArrayList<Integer> colores = new ArrayList<>();
        colores.add(Color.rgb(76, 175, 80)); // Verde para asistencias
        colores.add(Color.rgb(244, 67, 54)); // Rojo para faltas
        dataSet.setColors(colores);

        // 4. Agregar los datos a la gráfica
        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter(graficaPastel));
        data.setValueTextSize(14f);
        data.setValueTextColor(Color.WHITE);

        graficaPastel.setData(data);
        graficaPastel.invalidate(); // Refrescar la gráfica

        int asistencias = 85;
        int faltas = 15;

        txtAsistencia.setText(asistencias + "%");
        txtFaltas.setText(faltas + "%");
    }
}
