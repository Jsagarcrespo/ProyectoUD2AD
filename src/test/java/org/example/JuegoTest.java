package org.example.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class JuegoTest {

    @Test
    public void probarJuegoCorrecto() {
        // Termina sin lanzar excepciones.
        // El juego tiene nombre, autores, rango, duracion, tipo y estado validos.
        Juego juego = new Juego(1, "Juego A", "Autor A", "Descripcion", 2, 6, 60, "Estrategia", "competitivo", "Estanteria", "disponible", null, LocalDate.of(2026, 11, 1));
        assertDoesNotThrow(() -> juego.validar());
    }

    @Test
    public void probarNombreVacio() {

        // Lanza IllegalArgumentException.
        // El nombre del juego esta vacio.
        Juego juego = new Juego(1, "", "Autor A", "Descripcion", 2, 6, 60, "Estrategia", "competitivo", "Estanteria", "disponible", null, LocalDate.of(2026, 11, 1));
        assertThrows(IllegalArgumentException.class, () -> juego.validar());
    }

    @Test
    public void probarRangoJugadoresIncorrecto() {
        // Lanza IllegalArgumentException.
        // No puede haber un minimo de 7 y un maximo de 4 jugadores.

        Juego juego = new Juego(1, "Juego A", "Autor A", "Descripcion", 7, 4, 60, "Estrategia", "competitivo", "Estanteria", "disponible", null, LocalDate.of(2026, 11, 1));
        assertThrows(IllegalArgumentException.class, () -> juego.validar());
    }

    @Test
    public void probarDuracionIncorrecta() {
        // Lanza IllegalArgumentException.
        // La duracion debe ser mayor que cero y aqui vale 0.

        Juego juego = new Juego(1, "Juego A", "Autor A", "Descripcion", 2, 6, 0, "Estrategia", "competitivo", "Estanteria", "disponible", null, LocalDate.of(2026, 11, 1));
        assertThrows(IllegalArgumentException.class, () -> juego.validar());
    }

    @Test
    public void probarTipoIncorrecto() {
        // Lanza IllegalArgumentException.
        // El tipo otro no es competitivo, cooperativo ni mixto.

        Juego juego = new Juego(1, "Juego A", "Autor A", "Descripcion", 2, 6, 60, "Estrategia", "otro", "Estanteria", "disponible", null, LocalDate.of(2026, 11, 1));
        assertThrows(IllegalArgumentException.class, () -> juego.validar());
    }

    @Test
    public void probarEstadoIncorrecto() {
        // Lanza IllegalArgumentException.
        // El estado desconocido no es valido para un juego.

        Juego juego = new Juego(1, "Juego A", "Autor A", "Descripcion", 2, 6, 60, "Estrategia", "competitivo", "Estanteria", "desconocido", null, LocalDate.of(2026, 11, 1));
        assertThrows(IllegalArgumentException.class, () -> juego.validar());
    }
}
