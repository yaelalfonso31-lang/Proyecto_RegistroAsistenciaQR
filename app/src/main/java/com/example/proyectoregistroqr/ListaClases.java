package com.example.proyectoregistroqr;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import androidx.annotation.NonNull;
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

public class ListaClases extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ClaseAdapter adapter;
    private List<Clase> listaClases;
    private FloatingActionButton fabNuevaClase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_clases);

        recyclerView = findViewById(R.id.recyclerViewClases);
        fabNuevaClase = findViewById(R.id.fabNuevaClase);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaClases = new ArrayList<>();

        adapter = new ClaseAdapter(listaClases, clase -> {
            Intent intent = new Intent(ListaClases.this, pantallaMenu.class);
            intent.putExtra("rol", "admin");
            intent.putExtra("nrc", clase.getNrc());
            intent.putExtra("nombreClase", clase.getNombre());
            startActivity(intent);
        });

        recyclerView.setAdapter(adapter);
        fabNuevaClase.setOnClickListener(v -> startActivity(new Intent(this, CrearClase.class)));

        cargarClasesFirebase();
    }

    private void cargarClasesFirebase() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Clases");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listaClases.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Clase clase = dataSnapshot.getValue(Clase.class);
                    if (clase != null) {
                        listaClases.add(clase);
                    }
                }
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(ListaClases.this, "Error al cargar clases", Toast.LENGTH_SHORT).show();
            }
        });
    }
}