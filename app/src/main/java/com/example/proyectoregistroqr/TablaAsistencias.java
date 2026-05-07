package com.example.proyectoregistroqr;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class TablaAsistencias {
    private String id;
    private String matricula;
    private String nombre;
    private String fecha;
    private String hora;

    public TablaAsistencias(String id, String matricula, String nombre, String fecha, String hora) {
        this.id = id;
        this.matricula = matricula;
        this.nombre = nombre;
        this.fecha = fecha;
        this.hora = hora;
    }

    public String getId() { return id; }
    public String getMatricula() { return matricula; }
    public void setMatricula(String matricula) { this.matricula = matricula; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getHora() { return hora; }
    public void setHora(String hora) { this.hora = hora; }

    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }
}