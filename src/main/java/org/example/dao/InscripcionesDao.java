package org.example.dao;

import org.example.conexion.ConexionBD;
import org.example.modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionInscripciones {
    public static List<Inscripcion> listar(int partida) throws SQLException {
        List<Inscripcion> lista = new ArrayList<>();
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(
                     "SELECT * FROM inscripciones WHERE id_partida=? ORDER BY id")) {
            p.setInt(1, partida);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) lista.add(new Inscripcion(r.getInt("id"),
                        r.getInt("id_partida"), r.getString("documento_socia"),
                        r.getString("nombre_invitada"), r.getBoolean("ganadora")));
            }
        }
        return lista;
    }

    public static void inscribirSocia(int partida, String documento) throws SQLException {
        inscribir(new Inscripcion(0, partida, documento, null, false));
    }

    public static void inscribirInvitada(int partida, String nombre) throws SQLException {
        inscribir(new Inscripcion(0, partida, null, nombre, false));
    }

    public static void inscribir(Inscripcion inscripcion) throws SQLException {
        inscripcion.validar();
        try (Connection c = ConexionBD.conectar()) {
            c.setAutoCommit(false);
            try {
                // FOR UPDATE bloquea la partida hasta confirmar o cancelar la inscripción.
                int limite;
                String estado;
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT jugadores_deseados,estado FROM partidas WHERE id=? FOR UPDATE")) {
                    p.setInt(1, inscripcion.getIdPartida());
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) throw new IllegalArgumentException("La partida no existe.");
                        limite = r.getInt(1);
                        estado = r.getString(2);
                    }
                }
                if (!"programada".equals(estado))
                    throw new IllegalArgumentException("Solo se admiten inscripciones en partidas programadas.");
                int apuntadas;
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT COUNT(*) FROM inscripciones WHERE id_partida=?")) {
                    p.setInt(1, inscripcion.getIdPartida());
                    try (ResultSet r = p.executeQuery()) { r.next(); apuntadas = r.getInt(1); }
                }
                if (apuntadas >= limite) throw new IllegalArgumentException("No quedan plazas libres.");
                try (PreparedStatement p = c.prepareStatement(
                        "INSERT INTO inscripciones(id_partida,documento_socia,nombre_invitada) VALUES(?,?,?)")) {
                    p.setInt(1, inscripcion.getIdPartida());
                    if (inscripcion.getDocumentoSocia() == null) p.setNull(2, Types.VARCHAR);
                    else p.setString(2, inscripcion.getDocumentoSocia());
                    if (inscripcion.getNombreInvitada() == null) p.setNull(3, Types.VARCHAR);
                    else p.setString(3, inscripcion.getNombreInvitada());
                    p.executeUpdate();
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    public static boolean quitar(int id) throws SQLException {
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement("DELETE FROM inscripciones WHERE id=?")) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    public static boolean marcarGanadora(int id, boolean ganadora) throws SQLException {
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement("UPDATE inscripciones SET ganadora=? WHERE id=?")) {
            p.setBoolean(1, ganadora);
            p.setInt(2, id);
            return p.executeUpdate() > 0;
        }
    }

    public static String participantes(int idPartida) throws SQLException {
        String sql = "SELECT i.id,COALESCE(CONCAT(s.nombre,' ',s.apellidos),i.nombre_invitada) " +
                "AS nombre,i.documento_socia,i.ganadora FROM inscripciones i " +
                "LEFT JOIN socias s ON i.documento_socia=s.documento " +
                "WHERE i.id_partida=? ORDER BY i.id";
        StringBuilder b = new StringBuilder();
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, idPartida);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) b.append("Inscripción ").append(r.getInt(1)).append(" - ")
                        .append(r.getString(2)).append(r.getString(3) == null ? " (invitada)" : " (socia)")
                        .append(r.getBoolean(4) ? " - GANADORA" : "").append("\n");
            }
        }
        return b.toString();
    }

    public static Inscripcion buscar(int id) throws SQLException {
        String sql = "SELECT * FROM inscripciones WHERE id = ?";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) return null;
                return new Inscripcion(r.getInt("id"), r.getInt("id_partida"),
                        r.getString("documento_socia"), r.getString("nombre_invitada"),
                        r.getBoolean("ganadora"));
            }
        }
    }

    // Historial de inscripciones de una socia.
    public static List<Inscripcion> listarPorSocia(String documento) throws SQLException {
        List<Inscripcion> lista = new ArrayList<>();
        String sql = "SELECT * FROM inscripciones WHERE documento_socia = ? ORDER BY id_partida";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, documento);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) lista.add(new Inscripcion(r.getInt("id"),
                        r.getInt("id_partida"), r.getString("documento_socia"),
                        r.getString("nombre_invitada"), r.getBoolean("ganadora")));
            }
        }
        return lista;
    }
}
