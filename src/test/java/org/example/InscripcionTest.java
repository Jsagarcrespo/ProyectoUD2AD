package org.example.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InscripcionTest {

    @Test
    public void probarInscripcionSocioCorrecta() {

        // Termina sin lanzar excepciones.
        // Hay ID de partida y documento de socio, pero no invitado.
        Inscripcion inscripcion = new Inscripcion(1, 2, "12345678A", null, false);
        assertDoesNotThrow(() -> inscripcion.validar());
    }

    @Test
    public void probarInscripcionInvitadoCorrecta() {
        // Termina sin lanzar excepciones.
        // Hay ID de partida y nombre de invitado, pero no documento de socio.
        Inscripcion inscripcion = new Inscripcion(1, 2, null, "Invitado 1", false);
        assertDoesNotThrow(() -> inscripcion.validar());
    }

    @Test
    public void probarSocioEInvitadoALaVez() {

        // Lanza IllegalArgumentException.
        // Una inscripcion no puede ser de un socio y de un invitado a la vez.

        Inscripcion inscripcion = new Inscripcion(1, 2, "12345678A", "Invitado 1", false);
        assertThrows(IllegalArgumentException.class, () -> inscripcion.validar());
    }

    @Test
    public void probarInscripcionSinParticipante() {
        // Lanza IllegalArgumentException.
        // Falta tanto el socio como el nombre del invitado.
        Inscripcion inscripcion = new Inscripcion(1, 2, null, null, false);
        assertThrows(IllegalArgumentException.class, () -> inscripcion.validar());
    }

    @Test
    public void probarPartidaIncorrecta() {
        // Lanza IllegalArgumentException.
        // El identificador de partida es 0 y debe ser mayor que cero.
        Inscripcion inscripcion = new Inscripcion(1, 0, "12345678A", null, false);
        assertThrows(IllegalArgumentException.class, () -> inscripcion.validar());
    }
}
