package org.example.models;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AsociacionTest {

    @Test
    public void probarAsociacionCorrecta() {

        // Termina sin lanzar excepciones.
        // La razon social y el CIF estan informados.

        Asociacion asociacion = new Asociacion(1, "Asociacion A", "G12345678", "Calle A", "945000000", "info@ejemplo.com", LocalDate.of(2026, 1, 1));
        assertDoesNotThrow(() -> asociacion.validar());
    }

    @Test
    public void probarRazonSocialVacia() {

        // Lanza IllegalArgumentException.
        // La razon social esta vacia y es obligatoria.
        Asociacion asociacion = new Asociacion(1, "", "G12345678", "Calle A", "945000000", "info@ejemplo.com", LocalDate.of(2026, 1, 1));

        assertThrows(IllegalArgumentException.class, () -> asociacion.validar());
    }

    @Test
    public void probarCifVacio() {

        // Lanza IllegalArgumentException.
        // El CIF esta vacio y es obligatorio.
        Asociacion asociacion = new Asociacion(1, "Asociacion A", "", "Calle A", "945000000", "info@ejemplo.com", LocalDate.of(2026, 1, 1));
        assertThrows(IllegalArgumentException.class, () -> asociacion.validar());
    }
}
