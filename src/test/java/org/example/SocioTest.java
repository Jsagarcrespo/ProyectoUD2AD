package org.example.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class SocioTest {

    @Test
    public void probarSocioCorrecto() {
        // Termina sin lanzar excepciones.
        // Los campos obligatorios tienen valor y la edad es correcta.
        Socio socio = new Socio("12345678A", "Socio", "Prueba", "600000000", 25, "socio@ejemplo.com");
        assertDoesNotThrow(() -> socio.validar());
    }

    @Test
    public void probarDocumentoVacio() {
        // Lanza IllegalArgumentException.
        // El documento es obligatorio y lo hemos dejado vacio.
        Socio socio = new Socio("", "Socio", "Prueba", "600000000", 25, "socio@ejemplo.com");
        assertThrows(IllegalArgumentException.class, () -> socio.validar());
    }

    @Test
    public void probarNombreVacio() {
        // Lanza IllegalArgumentException.
        // El nombre es obligatorio y lo hemos dejado vacio.
        Socio socio = new Socio("12345678A", "", "Prueba", "600000000", 25, "socio@ejemplo.com");
        assertThrows(IllegalArgumentException.class, () -> socio.validar());
    }

    @Test
    public void probarEdadNegativa() {
        // Lanza IllegalArgumentException.
        // La edad es -1 y no puede ser negativa.
        Socio socio = new Socio("12345678A", "Socio", "Prueba", "600000000", -1, "socio@ejemplo.com");

        assertThrows(IllegalArgumentException.class, () -> socio.validar());
    }

    @Test
    public void probarEmailVacio() {

        // Lanza IllegalArgumentException.
        // El correo electronico es obligatorio y esta vacio.

        Socio socio = new Socio("12345678A", "Socio", "Prueba", "600000000", 25, "");

        assertThrows(IllegalArgumentException.class, () -> socio.validar());
    }
}
