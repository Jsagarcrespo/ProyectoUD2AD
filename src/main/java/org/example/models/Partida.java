package org.example.models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Partida {
    private int id;
    private int idJuego;
    private LocalDate fecha;
    private LocalTime hora;
    private int jugadoresDeseados;
    private String estado;

    public Partida(int id, int idJuego, LocalDate fecha, LocalTime hora, int jugadoresDeseados, String estado) {
        this.id = id;
        this.idJuego = idJuego;
        this.fecha = fecha;
        this.hora = hora;
        this.jugadoresDeseados = jugadoresDeseados;
        this.estado = estado;
    }

    public void validar(int min, int max) {
        if (idJuego <= 0 || fecha == null || hora == null)
            throw new IllegalArgumentException("Juego, fecha y hora son obligatorios.");
        if (jugadoresDeseados < min || jugadoresDeseados > max)
            throw new IllegalArgumentException("El numero de jugadoras debe estar entre " + min + " y " + max + ".");
        if (!("programada".equals(estado) || "finalizada".equals(estado) || "cancelada".equals(estado)))
            throw new IllegalArgumentException("Estado de partida incorrecto.");
    }

    public static int plazasLibres(int deseadas, int inscritas) {
        return Math.max(0, deseadas - inscritas);
    }

    public int getId() {
        return id;
    }

    public int getIdJuego() {
        return idJuego;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public int getJugadoresDeseados() {
        return jugadoresDeseados;
    }

    public String getEstado() {
        return estado;
    }
}
