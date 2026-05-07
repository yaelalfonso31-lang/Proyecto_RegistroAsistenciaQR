package com.example.proyectoregistroqr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class Adaptador extends RecyclerView.Adapter<Adaptador.ViewHolder> {

    private List<TablaAsistencias> listaAsistencias;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onEditarClick(int position);
        void onEliminarClick(int position);
    }

    public Adaptador(List<TablaAsistencias> listaAsistencias, OnItemClickListener listener) {
        this.listaAsistencias = listaAsistencias;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Vinculamos tu layout específico para la fila
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_tabla_asistencias, parent, false);
        return new ViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TablaAsistencias asistencia = listaAsistencias.get(position);

        holder.tvMatricula.setText(asistencia.getMatricula());
        holder.tvNombre.setText(asistencia.getNombre()); // Muestra el nombre
        holder.tvFecha.setText(asistencia.getFecha());
        holder.tvHora.setText(asistencia.getHora());     // Muestra la hora
    }

    @Override
    public int getItemCount() {
        return listaAsistencias.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMatricula, tvNombre, tvFecha, tvHora;
        ImageButton btnEditar, btnEliminar;

        public ViewHolder(@NonNull View itemView, OnItemClickListener listener) {
            super(itemView);
            tvMatricula = itemView.findViewById(R.id.tvMatricula);
            tvFecha = itemView.findViewById(R.id.tvFecha);
            tvNombre = itemView.findViewById(R.id.tvNombre);
            tvHora = itemView.findViewById(R.id.tvHora);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnEliminar = itemView.findViewById(R.id.btnEliminar);

            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEditarClick(position);
                    }
                }
            });

            btnEliminar.setOnClickListener(v -> {
                if (listener != null) {
                    int position = getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        listener.onEliminarClick(position);
                    }
                }
            });
        }
    }
}