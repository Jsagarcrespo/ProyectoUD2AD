package org.example.models;

import java.time.LocalDate;

public class Juego {

    private int id;
    private String nombre;
    private String autores;
    private String descripcion;

    private int minJugadores;
    private int maxJugadores;
    private int duracionMin;

    private String tematica;
    private String tipo;
    private String ubicacion;
    private String estado;
    private String documentoPropietario;
    private LocalDate fechaAlta;


    // Constructor
    public Juego(int id, String nombre, String autores, String descripcion, int minJugadores, int maxJugadores, int duracionMin, String tematica, String tipo, String ubicacion, String estado, String documentoPropietario, LocalDate fechaAlta) {

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
        this.documentoPropietario = documentoPropietario;
        this.fechaAlta = fechaAlta;
    }


    // Comprueba que los datos del juego son correctos
    public void validar() {

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre del juego es obligatorio");
        }

        if (autores == null || autores.isBlank()) {
            throw new IllegalArgumentException("Los autores son obligatorios");
        }

        if (minJugadores < 1 || maxJugadores < minJugadores) {
            throw new IllegalArgumentException("El rango de jugadores es incorrecto");
        }

        if (duracionMin <= 0) {
            throw new IllegalArgumentException("La duracion debe ser mayor que cero");
        }

        if (!("competitivo".equals(tipo) || "cooperativo".equals(tipo) || "mixto".equals(tipo))) {

            throw new IllegalArgumentException("Tipo de juego incorrecto");
        }

        if (!("disponible".equals(estado) || "prestado".equals(estado) || "reparacion".equals(estado))) {

            throw new IllegalArgumentException("Estado del juego incorrecto");
        }

        if (fechaAlta == null) {
            throw new IllegalArgumentException("La fecha de alta es obligatoria");
        }
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

    public String getDocumentoPropietario() {
        return documentoPropietario;
    }

    public LocalDate getFechaAlta() {
        return fechaAlta;
    }
}
