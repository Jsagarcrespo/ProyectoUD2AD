package org.example.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class PartidaTest {

    // Estas tres pruebas son las que ya tenias
    @Test
    public void probarPlazasLibres() {
        // Quedan 2 plazas libres (4 plazas - 2 inscritos).
        // JUnit debe marcar la prueba en verde porque resultado es 2.
        int resultado = Partida.plazasLibres(4, 2);
        assertEquals(2, resultado);
    }

    @Test
    public void probarPartidaLlena() {
        // 0 plazas libres porque las 4 estan ocupadas.
        int resultado = Partida.plazasLibres(4, 4);
        assertEquals(0, resultado);
    }

    @Test
    public void probarQueNoHayPlazasNegativas() {
        //  0 plazas libres, nunca un numero negativo, aunque haya 6 inscritos.
        int resultado = Partida.plazasLibres(4, 6);
        assertEquals(0, resultado);
    }

    @Test
    public void probarPartidaCorrecta() {
        // Termina sin lanzar excepciones.
        // Los 4 jugadores estan dentro del rango y el estado es programada.
        Partida partida = new Partida(1, 2, LocalDate.of(2026, 11, 1), LocalTime.of(18, 0), 4, "programada");
        assertDoesNotThrow(() -> partida.validar(2, 6));
    }

    @Test
    public void probarJugadoresFueraDelRango() {

        // valida(2, 6) lanza IllegalArgumentException.
        // Se solicitan 8 jugadores, pero el maximo permitido es 6.
        Partida partida = new Partida(1, 2, LocalDate.of(2026, 11, 1), LocalTime.of(18, 0), 8, "programada");
        assertThrows(IllegalArgumentException.class, () -> partida.validar(2, 6));
    }

    @Test
    public void probarEstadoIncorrecto() {
        // Valida(2, 6) lanza IllegalArgumentException.
        // El estado desconocido no es uno de los estados admitidos.
        Partida partida = new Partida(1, 2, LocalDate.of(2026, 11, 1), LocalTime.of(18, 0), 4, "desconocido");
        assertThrows(IllegalArgumentException.class, () -> partida.validar(2, 6));
    }
}
