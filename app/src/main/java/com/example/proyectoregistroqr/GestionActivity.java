package com.example.proyectoregistroqr;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class GestionActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private Adaptador adapter;
    private List<TablaAsistencias> listaAsistencias;
    private DatabaseReference myRef;
    private Button btnAtras;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gestion);

        btnAtras = findViewById(R.id.btnAtras);

        btnAtras.setOnClickListener(v -> {
            Intent intent = new Intent(GestionActivity.this, pantallaMenu.class);
            startActivity(intent);
        });

        // 1. Inicializar la conexión a la base de datos de Firebase
        myRef = FirebaseDatabase.getInstance().getReference("Asistencias");

        // 2. Configurar el RecyclerView
        recyclerView = findViewById(R.id.recyclerViewAsistencias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        listaAsistencias = new ArrayList<>();

        // 3. Configurar el botón flotante para crear un registro manual
        FloatingActionButton fabNuevo = findViewById(R.id.fabNuevoRegistro);
        fabNuevo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(GestionActivity.this, NuevoRegistro.class);
                startActivity(intent);
            }
        });

        // 4. Configurar el Adaptador y sus acciones (Editar y Eliminar)
        adapter = new Adaptador(listaAsistencias, new Adaptador.OnItemClickListener() {
            @Override
            public void onEditarClick(int position) {
                TablaAsistencias asistenciaSeleccionada = listaAsistencias.get(position);

                Intent intent = new Intent(GestionActivity.this, EditarRegistro.class);
                intent.putExtra("id", asistenciaSeleccionada.getId());
                intent.putExtra("matricula", asistenciaSeleccionada.getMatricula());
                intent.putExtra("nombre", asistenciaSeleccionada.getNombre());
                intent.putExtra("fecha", asistenciaSeleccionada.getFecha());
                intent.putExtra("hora", asistenciaSeleccionada.getHora());

                startActivity(intent);
            }

            @Override
            public void onEliminarClick(int position) {
                mostrarDialogoEliminar(position);
            }
        });

        recyclerView.setAdapter(adapter);

        // 5. Leer los datos desde Firebase en tiempo real
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaAsistencias.clear(); // Limpiamos la lista para evitar datos duplicados en pantalla
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    TablaAsistencias asistencia = dataSnapshot.getValue(TablaAsistencias.class);
                    if (asistencia != null) {
                        listaAsistencias.add(asistencia);
                    }
                }
                adapter.notifyDataSetChanged(); // Refrescamos la tabla con los datos nuevos
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(GestionActivity.this, "Error al cargar datos desde la nube", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void mostrarDialogoEliminar(int position) {
        // 1. Obtenemos el ID único generado por Firebase para este registro
        String idABorrar = listaAsistencias.get(position).getId();

        // 2. Apuntamos exactamente al nodo de este registro en la base de datos
        DatabaseReference myRef = FirebaseDatabase.getInstance().getReference("Asistencias").child(idABorrar);

        // 3. Construimos la ventana emergente
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Eliminación");
        builder.setMessage("¿Estás seguro de que deseas eliminar este registro?");

        // 4. Qué hacer si el usuario presiona "Eliminar"
        builder.setPositiveButton("Eliminar", (dialog, which) -> {
            // Borramos el dato de la base de datos en la nube
            myRef.removeValue()
                    .addOnSuccessListener(aVoid -> {
                        // Si se borró bien en la nube, mostramos un mensaje
                        Toast.makeText(GestionActivity.this, "Registro eliminado correctamente", Toast.LENGTH_SHORT).show();
                        // (Ojo: No necesitas borrar de listaAsistencias manualmente aquí,
                        // porque el ValueEventListener de tu onCreate detectará el cambio y actualizará la tabla automáticamente)
                    })
                    .addOnFailureListener(e -> {
                        // Si hubo un error de conexión, avisamos
                        Toast.makeText(GestionActivity.this, "Error al eliminar el registro", Toast.LENGTH_SHORT).show();
                    });
        });

        // 5. Qué hacer si el usuario presiona "Cancelar"
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        // 6. Mostramos el diálogo en pantalla
        builder.show();
    }
}