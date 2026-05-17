package com.example.proyectoregistroqr;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
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
    private MaterialButton fabNuevaClase;
    private MaterialButton btnCerrarSesion;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_clases);

        recyclerView = findViewById(R.id.recyclerViewClases);
        fabNuevaClase = findViewById(R.id.fabNuevaClase);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaClases = new ArrayList<>();

        btnCerrarSesion = findViewById(R.id.btnCerrarSesionClases);
        btnCerrarSesion.setOnClickListener(v -> ejecutarCierreDeSesion());

        // Configuración ampliada del Listener para procesar las 3 acciones
        adapter = new ClaseAdapter(listaClases, new ClaseAdapter.OnClaseClickListener() {
            @Override
            public void onClaseClick(Clase clase) {
                // Abre el menú pasándole los datos de control contextuales
                Intent intent = new Intent(ListaClases.this, pantallaMenu.class);
                intent.putExtra("rol", "admin");
                intent.putExtra("nrc", clase.getNrc());
                intent.putExtra("nombreClase", clase.getNombre());
                startActivity(intent);
            }

            @Override
            public void onEditarClick(Clase clase) {
                mostrarDialogoEditar(clase);
            }

            @Override
            public void onEliminarClick(Clase clase) {
                mostrarDialogoEliminar(clase);
            }
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

    // Cuadro de diálogo interactivo con formulario integrado para edición rápida
    private void mostrarDialogoEditar(Clase clase) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Información de Clase");

        // Contenedor dinámico estructurado verticalmente para los campos
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText etNombre = new EditText(this);
        etNombre.setHint("Nombre de la clase");
        etNombre.setText(clase.getNombre());
        layout.addView(etNombre);

        final EditText etNrc = new EditText(this);
        etNrc.setHint("NRC (5 dígitos)");
        etNrc.setText(clase.getNrc());
        etNrc.setInputType(InputType.TYPE_CLASS_NUMBER);
        etNrc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)}); // Forzar límite estricto de 5 números
        layout.addView(etNrc);

        final EditText etEstudiantes = new EditText(this);
        etEstudiantes.setHint("Estudiantes inscritos");
        etEstudiantes.setText(String.valueOf(clase.getEstudiantesInscritos()));
        etEstudiantes.setInputType(InputType.TYPE_CLASS_NUMBER);
        layout.addView(etEstudiantes);

        final EditText etHorario = new EditText(this);
        etHorario.setHint("Horario (Ej: Lun-Mie 09:00 AM)");
        etHorario.setText(clase.getHorario());
        layout.addView(etHorario);

        builder.setView(layout);

        builder.setPositiveButton("Guardar", (dialog, which) -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevoNrc = etNrc.getText().toString().trim();
            String estStr = etEstudiantes.getText().toString().trim();
            String nuevoHorario = etHorario.getText().toString().trim();

            // Validaciones de negocio idénticas a la de creación
            if (nuevoNombre.isEmpty() || nuevoNrc.length() != 5 || estStr.isEmpty() || nuevoHorario.isEmpty()) {
                Toast.makeText(this, "Campos inválidos o incompletos", Toast.LENGTH_SHORT).show();
                return;
            }

            int nuevosEstudiantes = Integer.parseInt(estStr);

            // Apuntamos al nodo existente en la nube mediante su ID original inmutable
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clases").child(clase.getId());
            Clase claseActualizada = new Clase(clase.getId(), nuevoNombre, nuevoNrc, nuevosEstudiantes, nuevoHorario);

            ref.setValue(claseActualizada)
                    .addOnSuccessListener(aVoid -> Toast.makeText(ListaClases.this, "Materia modificada con éxito", Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(ListaClases.this, "Error de red al actualizar", Toast.LENGTH_SHORT).show());
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Ventana emergente nativa para confirmación de borrado permanente
    private void mostrarDialogoEliminar(Clase clase) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás completamente seguro de que deseas eliminar la materia \"" + clase.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clases").child(clase.getId());
                    ref.removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(ListaClases.this, "Materia eliminada de la base de datos", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(ListaClases.this, "Error de comunicación con la nube", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void ejecutarCierreDeSesion() {
        // 1. Vaciamos las preferencias locales por completo
        getSharedPreferences("SESION", MODE_PRIVATE).edit().clear().apply();

        // 2. Redirigimos al Login destruyendo todo el historial de pantallas previo
        Intent intent = new Intent(ListaClases.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
        finish(); // Destruye esta actividad
    }
}