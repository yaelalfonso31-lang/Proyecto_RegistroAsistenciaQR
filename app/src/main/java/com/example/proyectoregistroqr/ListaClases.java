package com.example.proyectoregistroqr;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
    private com.google.android.material.button.MaterialButton fabNuevaClase, btnCerrarSesion;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_clases);

        recyclerView = findViewById(R.id.recyclerViewClases);
        fabNuevaClase = findViewById(R.id.fabNuevaClase);
        btnCerrarSesion = findViewById(R.id.btnCerrarSesionClases);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        listaClases = new ArrayList<>();

        adapter = new ClaseAdapter(listaClases, new ClaseAdapter.OnClaseClickListener() {
            @Override
            public void onClaseClick(Clase clase) {
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

        btnCerrarSesion.setOnClickListener(v -> {
            getSharedPreferences("SESION", MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(ListaClases.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            Toast.makeText(this, "Sesión cerrada correctamente", Toast.LENGTH_SHORT).show();
            finish();
        });

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

    // ========================================================
    // VENTANA EMERGENTE BLINDADA CON VALIDACIONES ESTRICTAS
    // ========================================================
    private void mostrarDialogoEditar(Clase clase) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Editar Información de Clase");

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
        etNrc.setFilters(new InputFilter[]{new InputFilter.LengthFilter(5)});
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

        // Se declara null inicialmente para que la ventana no se cierre automáticamente
        builder.setPositiveButton("Guardar", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();
        dialog.show();

        // Interceptamos el botón después de mostrar la ventana
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String nuevoNombre = etNombre.getText().toString().trim();
            String nuevoNrc = etNrc.getText().toString().trim();
            String estStr = etEstudiantes.getText().toString().trim();
            String nuevoHorario = etHorario.getText().toString().trim();

            // 1. Campos vacíos
            if (TextUtils.isEmpty(nuevoNombre) || TextUtils.isEmpty(nuevoNrc) || TextUtils.isEmpty(estStr) || TextUtils.isEmpty(nuevoHorario)) {
                Toast.makeText(this, "Por favor, completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }

            // 2. Validación del nombre
            if (!nuevoNombre.matches("^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$")) {
                etNombre.setError("Solo se permiten letras y espacios");
                etNombre.requestFocus();
                return; // La ventana NO se cerrará
            }

            // 3. Validación de NRC
            if (nuevoNrc.length() != 5) {
                etNrc.setError("El NRC debe tener exactamente 5 dígitos");
                etNrc.requestFocus();
                return;
            }

            // 4. Validación de estudiantes (1 - 100)
            int nuevosEstudiantes = 0;
            try {
                nuevosEstudiantes = Integer.parseInt(estStr);
                if (nuevosEstudiantes <= 0) {
                    etEstudiantes.setError("Debe haber al menos 1 estudiante");
                    etEstudiantes.requestFocus();
                    return;
                }
                if (nuevosEstudiantes > 100) {
                    etEstudiantes.setError("El máximo permitido es 100 alumnos");
                    etEstudiantes.requestFocus();
                    return;
                }
            } catch (NumberFormatException e) {
                etEstudiantes.setError("Número inválido");
                etEstudiantes.requestFocus();
                return;
            }

            // Si pasa todas las validaciones, guardamos en la nube
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clases").child(clase.getId());
            Clase claseActualizada = new Clase(clase.getId(), nuevoNombre, nuevoNrc, nuevosEstudiantes, nuevoHorario);

            ref.setValue(claseActualizada)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(ListaClases.this, "Materia modificada con éxito", Toast.LENGTH_SHORT).show();
                        dialog.dismiss(); // Solo cerramos la ventana si se guardó correctamente
                    })
                    .addOnFailureListener(e -> Toast.makeText(ListaClases.this, "Error de red al actualizar", Toast.LENGTH_SHORT).show());
        });
    }

    private void mostrarDialogoEliminar(Clase clase) {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar Eliminación")
                .setMessage("¿Estás seguro de que deseas eliminar la materia \"" + clase.getNombre() + "\"?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Clases").child(clase.getId());
                    ref.removeValue()
                            .addOnSuccessListener(aVoid -> Toast.makeText(ListaClases.this, "Materia eliminada", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(ListaClases.this, "Error de comunicación", Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}