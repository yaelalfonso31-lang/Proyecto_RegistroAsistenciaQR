package com.example.proyectoregistroqr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import android.widget.Toast;

public class GestionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Adaptador adapter;
    private List<TablaAsistencias> listaAsistencias;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion);
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("Asistencias");

        recyclerView = findViewById(R.id.recyclerViewAsistencias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TablaAsistencias nuevaAsistencia = new TablaAsistencias(
                "ID_PRUEBA_1",
                "201912345",
                "Yael Serrano",
                "10/05/2026",
                "08:00 AM"
        );
        myRef.push().setValue(nuevaAsistencia)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "¡Conexión exitosa! Dato guardado en Firebase", Toast.LENGTH_LONG).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al guardar: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });

        // Agregamos algunos datos de prueba para que puedas ver la tabla llena de momento
        // Datos de prueba actualizados con 5 parámetros (ID, Matrícula, Nombre, Fecha, Hora)
        listaAsistencias = new ArrayList<>();
        listaAsistencias.add(new TablaAsistencias("1", "201912345", "Juan Pérez", "2026-05-06", "08:00 AM"));
        listaAsistencias.add(new TablaAsistencias("2", "202054321", "María López", "2026-05-06", "08:15 AM"));
        listaAsistencias.add(new TablaAsistencias("3", "202111222", "Carlos Ruiz", "2026-05-07", "09:00 AM"));
        listaAsistencias.add(new TablaAsistencias("4", "202233444", "Ana García", "2026-05-07", "09:15 AM"));
        listaAsistencias.add(new TablaAsistencias("5", "202355666", "Luis Martínez", "2026-05-08", "10:00 AM"));

        adapter = new Adaptador(listaAsistencias, new Adaptador.OnItemClickListener() {
            @Override
            public void onEditarClick(int position) {
                mostrarDialogoEditar(position);
            }

            @Override
            public void onEliminarClick(int position) {
                mostrarDialogoEliminar(position);
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void mostrarDialogoEditar(int position) {
        TablaAsistencias asistencia = listaAsistencias.get(position);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Asistencia");

        final EditText inputMatricula = new EditText(this);
        inputMatricula.setText(asistencia.getMatricula());
        builder.setView(inputMatricula);

        builder.setPositiveButton("Actualizar", (dialog, which) -> {
            String nuevaMatricula = inputMatricula.getText().toString();
            asistencia.setMatricula(nuevaMatricula);
            adapter.notifyItemChanged(position);
            Toast.makeText(GestionActivity.this, "Registro actualizado", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void mostrarDialogoEliminar(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Eliminación");
        builder.setMessage("¿Estás seguro de que deseas eliminar este registro?");

        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            listaAsistencias.remove(position);
            adapter.notifyItemRemoved(position);
            Toast.makeText(GestionActivity.this, "Registro eliminado", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }
}