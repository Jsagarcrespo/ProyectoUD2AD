package org.example.models;

public class Inscripcion {

    private int id;
    private int idPartida;

    private String documentoSocio;
    private String nombreInvitado;

    private boolean ganador;


    // Constructor
    public Inscripcion(int id, int idPartida, String documentoSocio, String nombreInvitado, boolean ganador) {

        this.id = id;
        this.idPartida = idPartida;
        this.documentoSocio = documentoSocio;
        this.nombreInvitado = nombreInvitado;
        this.ganador = ganador;
    }


    // Comprueba que la inscripcion es correcta
    public void validar() {

        boolean socio = documentoSocio != null && !documentoSocio.isBlank();

        boolean invitado = nombreInvitado != null && !nombreInvitado.isBlank();

        if (idPartida <= 0) {
            throw new IllegalArgumentException("La partida no es valida");
        }

        if (socio == invitado) {
            throw new IllegalArgumentException("La inscripcion debe ser de un socio " + "o de un invitado, pero no de ambos");
        }
    }


    public int getId() {
        return id;
    }

    public int getIdPartida() {
        return idPartida;
    }

    public String getDocumentoSocio() {
        return documentoSocio;
    }

    public String getNombreInvitado() {
        return nombreInvitado;
    }

    public boolean isGanador() {
        return ganador;
    }
}
