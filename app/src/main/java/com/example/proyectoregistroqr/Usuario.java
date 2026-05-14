package com.example.proyectoregistroqr;

public class Usuario {
    private String id;
    private String nombre;
    private String correo;
    private String contrasena;
    private String rol; // "admin" o "alumno"

    public Usuario() {} // Constructor vacío para Firebase

    public Usuario(String id, String nombre, String correo, String contrasena, String rol) {
        this.id = id;
        this.nombre = nombre;
        this.correo = correo;
        this.contrasena = contrasena;
        this.rol = rol;
    }

    // Getters y Setters
    public String getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCorreo() { return correo; }
    public String getContrasena() { return contrasena; }
    public String getRol() { return rol; }
}