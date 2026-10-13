package org.example.models;

public class Inscripcion {
    private int id;
    private int idPartida;
    private String documentoSocia;
    private String nombreInvitada;
    private boolean ganadora;

    public Inscripcion(int id, int idPartida, String documentoSocia, String nombreInvitada, boolean ganadora) {
        this.id = id;
        this.idPartida = idPartida;
        this.documentoSocia = documentoSocia;
        this.nombreInvitada = nombreInvitada;
        this.ganadora = ganadora;
    }

    public void validar() {
        boolean socia = documentoSocia != null && !documentoSocia.isBlank();
        boolean invitada = nombreInvitada != null && !nombreInvitada.isBlank();
        if (idPartida <= 0 || socia == invitada)
            throw new IllegalArgumentException("La inscripcion debe ser de una socia O de una invitada.");
    }

    public int getId() { return id; }
    public int getIdPartida() { return idPartida; }
    public String getDocumentoSocia() { return documentoSocia; }
    public String getNombreInvitada() { return nombreInvitada; }
    public boolean isGanadora() { return ganadora; }
}
