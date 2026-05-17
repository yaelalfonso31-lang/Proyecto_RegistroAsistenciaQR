package com.example.proyectoregistroqr;

public class Clase {
    private String id;
    private String nombre;
    private String nrc; // Código de 5 dígitos
    private int estudiantesInscritos;
    private String horario;

    public Clase() {} // Constructor vacío obligatorio para Firebase

    public Clase(String id, String nombre, String nrc, int estudiantesInscritos, String horario) {
        this.id = id;
        this.nombre = nombre;
        this.nrc = nrc;
        this.estudiantesInscritos = estudiantesInscritos;
        this.horario = horario;
    }

    // Getters y Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getNrc() { return nrc; }
    public void setNrc(String nrc) { this.nrc = nrc; }
    public int getEstudiantesInscritos() { return estudiantesInscritos; }
    public void setEstudiantesInscritos(int estudiantesInscritos) { this.estudiantesInscritos = estudiantesInscritos; }
    public String getHorario() { return horario; }
    public void setHorario(String horario) { this.horario = horario; }
}