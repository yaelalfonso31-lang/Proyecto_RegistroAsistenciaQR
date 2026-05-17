package com.example.proyectoregistroqr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClaseAdapter extends RecyclerView.Adapter<ClaseAdapter.ViewHolder> {

    private List<Clase> listaClases;
    private OnClaseClickListener listener;

    // Interfaz ampliada para soportar las tres operaciones de clic
    public interface OnClaseClickListener {
        void onClaseClick(Clase clase);
        void onEditarClick(Clase clase);
        void onEliminarClick(Clase clase);
    }

    public ClaseAdapter(List<Clase> listaClases, OnClaseClickListener listener) {
        this.listaClases = listaClases;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clase, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Clase clase = listaClases.get(position);
        holder.tvNombre.setText(clase.getNombre());
        holder.tvNrc.setText("NRC: " + clase.getNrc());
        holder.tvHorario.setText(clase.getHorario());
        holder.tvAlumnos.setText("Inscritos: " + clase.getEstudiantesInscritos());

        // Clic en toda la tarjeta (Abre el menú principal de la clase)
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClaseClick(clase);
        });

        // Clic específico en el icono de Editar
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditarClick(clase);
        });

        // Clic específico en el icono de Eliminar
        holder.btnEliminar.setOnClickListener(v -> {
            if (listener != null) listener.onEliminarClick(clase);
        });
    }

    @Override
    public int getItemCount() { return listaClases.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvNrc, tvHorario, tvAlumnos;
        ImageButton btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvItemNombreClase);
            tvNrc = itemView.findViewById(R.id.tvItemNrc);
            tvHorario = itemView.findViewById(R.id.tvItemHorario);
            tvAlumnos = itemView.findViewById(R.id.tvItemAlumnos);
            btnEditar = itemView.findViewById(R.id.btnEditarClase);
            btnEliminar = itemView.findViewById(R.id.btnEliminarClase);
        }
    }
}