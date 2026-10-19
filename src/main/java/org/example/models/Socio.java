package org.example.models;

public class Socio {

    private String documento;
    private String nombre;
    private String apellidos;
    private String telefono;
    private int edad;
    private String email;

    public Socio(String documento, String nombre, String apellidos, String telefono, int edad, String email) {

        this.documento = documento;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.telefono = telefono;
        this.edad = edad;
        this.email = email;
    }

    // Comprueba que los datos sean correctos
    public void validar() {

        if (documento == null || documento.isBlank()) {
            throw new IllegalArgumentException("El documento es obligatorio");
        }

        if (nombre == null || nombre.isBlank()) {
            throw new IllegalArgumentException("El nombre es obligatorio");
        }

        if (apellidos == null || apellidos.isBlank()) {
            throw new IllegalArgumentException("Los apellidos son obligatorios");
        }

        if (edad < 0) {
            throw new IllegalArgumentException("La edad no puede ser negativa");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email es obligatorio");
        }
    }

    public String getDocumento() {
        return documento;
    }

    public void setDocumento(String documento) {
        this.documento = documento;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public int getEdad() {
        return edad;
    }

    public void setEdad(int edad) {
        this.edad = edad;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
