package org.example.models;

import java.time.LocalDate;

public class Juego {
    private int id;
    private String nombre;
    private String autores;
    private String descripcion;
    private String tematica;
    private String tipo;
    private String ubicacion;
    private String estado;
    private String documentoPropietaria;
    private LocalDate fechaAlta;

    private int minJugadores;
    private int maxJugadores;
    private int duracionMin;


    public Juego(int id, String nombre, String autores, String descripcion,
                 int minJugadores, int maxJugadores, int duracionMin,
                 String tematica, String tipo, String ubicacion, String estado,
                 String documentoPropietaria, LocalDate fechaAlta) {
        this.id = id;
        this.nombre = nombre;
        this.autores = autores;
        this.descripcion = descripcion;
        this.minJugadores = minJugadores;
        this.maxJugadores = maxJugadores;
        this.duracionMin = duracionMin;
        this.tematica = tematica;
        this.tipo = tipo;
        this.ubicacion = ubicacion;
        this.estado = estado;
        this.documentoPropietaria = documentoPropietaria;
        this.fechaAlta = fechaAlta;
    }

    public void validar() {
        if (nombre == null || nombre.isEmpty() || autores == null || autores.isEmpty())
            throw new IllegalArgumentException("Nombre y autores son obligatorios.");
        if (minJugadores < 1 || maxJugadores < minJugadores)
            throw new IllegalArgumentException("Rango incorrecto.");
        if (duracionMin <= 0)
            throw new IllegalArgumentException("Duracion mayor a 0.");
        if (!("competitivo".equals(tipo) || "cooperativo".equals(tipo) || "mixto".equals(tipo)))
            throw new IllegalArgumentException("Tipo de juego incorrecto.");
        if (!("disponible".equals(estado) || "prestado".equals(estado) || "reparacion".equals(estado)))
            throw new IllegalArgumentException("Estado incorrecto.");
        if (fechaAlta == null)
            throw new IllegalArgumentException("La fecha de alta es obligatoria.");
    }

    public int getId() {
        return id;
    }
    public String getNombre() {
        return nombre;
    }
    public String getAutores() {
        return autores;
    }
    public String getDescripcion() {
        return descripcion;
    }
    public int getMinJugadores() {
        return minJugadores;
    }
    public int getMaxJugadores() {
        return maxJugadores;
    }
    public int getDuracionMin() {
        return duracionMin;
    }
    public String getTematica() {
        return tematica;
    }
    public String getTipo() {
        return tipo;
    }
    public String getUbicacion() {
        return ubicacion;
    }
    public String getEstado() {
        return estado;
    }
    public String getDocumentoPropietaria() {
        return documentoPropietaria;
    }
    public LocalDate getFechaAlta() {
        return fechaAlta;
    }
}
