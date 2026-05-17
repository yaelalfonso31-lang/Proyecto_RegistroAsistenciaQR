package com.example.proyectoregistroqr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ClaseAdapter extends RecyclerView.Adapter<ClaseAdapter.ViewHolder> {

    private List<Clase> listaClases;
    private OnClaseClickListener listener;

    public interface OnClaseClickListener {
        void onClaseClick(Clase clase);
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

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClaseClick(clase);
        });
    }

    @Override
    public int getItemCount() { return listaClases.size(); }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvNombre, tvNrc, tvHorario, tvAlumnos;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvNombre = itemView.findViewById(R.id.tvItemNombreClase);
            tvNrc = itemView.findViewById(R.id.tvItemNrc);
            tvHorario = itemView.findViewById(R.id.tvItemHorario);
            tvAlumnos = itemView.findViewById(R.id.tvItemAlumnos);
        }
    }
}