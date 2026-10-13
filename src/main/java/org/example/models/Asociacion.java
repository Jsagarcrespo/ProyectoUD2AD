package org.example.models;

import java.time.LocalDate;

public class Asociacion {
    private int id;
    private String razonSocial;
    private String cif;
    private String direccion;
    private String telefono;
    private String email;
    private LocalDate fechaApertura;

    public Asociacion(int id, String razonSocial, String cif, String direccion, String telefono, String email, LocalDate fechaApertura) {
        this.id = id;
        this.razonSocial = razonSocial;
        this.cif = cif;
        this.direccion = direccion;
        this.telefono = telefono;
        this.email = email;
        this.fechaApertura = fechaApertura;
    }

    public void validar() {
        if (razonSocial == null || razonSocial.isEmpty() || cif == null || cif.isEmpty())
            throw new IllegalArgumentException("Razon social y CIF obligatorios.");
    }
    public int getId() { return id; }
    public String getRazonSocial() { return razonSocial; }
    public String getCif() { return cif; }
    public String getDireccion() { return direccion; }
    public String getTelefono() { return telefono; }
    public String getEmail() { return email; }
    public LocalDate getFechaApertura() { return fechaApertura; }
}
