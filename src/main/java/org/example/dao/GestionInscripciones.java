
package org.example.dao;

import org.example.models.Inscripcion;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionInscripciones {

    // Convierte un registro en una inscripcion
    private static Inscripcion convertir(ResultSet r)
            throws SQLException {

        return new Inscripcion(
                r.getInt("id"),
                r.getInt("id_partida"),
                r.getString("documento_socio"),
                r.getString("nombre_invitado"),
                r.getBoolean("ganador")
        );
    }

    // Lista las inscripciones de una partida
    public static List<Inscripcion> listar(int idPartida)
            throws SQLException {

        List<Inscripcion> lista = new ArrayList<>();

        String sql = "SELECT * FROM inscripciones " +
                "WHERE id_partida=? ORDER BY id";

        try (Connection c = DatabaseConnection.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, idPartida);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }

    // Inscribe un socio
    public static void inscribirSocio(int idPartida, String documento)
            throws SQLException {

        Inscripcion inscripcion = new Inscripcion(
                0, idPartida, documento, null, false);

        inscribir(inscripcion);
    }

    // Inscribe un invitado
    public static void inscribirInvitado(int idPartida, String nombre)
            throws SQLException {

        Inscripcion inscripcion = new Inscripcion(
                0, idPartida, null, nombre, false);

        inscribir(inscripcion);
    }

    // Guarda una inscripcion comprobando las plazas
    public static void inscribir(Inscripcion inscripcion)
            throws SQLException {

        inscripcion.validar();

        try (Connection c = DatabaseConnection.conectar()) {

            c.setAutoCommit(false);

            try {

                int plazas;
                String estado;

                // Bloquea la partida durante la inscripcion
                String sqlPartida =
                        "SELECT jugadores_deseados, estado " +
                                "FROM partidas WHERE id=? FOR UPDATE";

                try (PreparedStatement p =
                             c.prepareStatement(sqlPartida)) {

                    p.setInt(1, inscripcion.getIdPartida());

                    try (ResultSet r = p.executeQuery()) {

                        if (!r.next()) {
                            throw new IllegalArgumentException(
                                    "La partida no existe");
                        }

                        plazas = r.getInt("jugadores_deseados");
                        estado = r.getString("estado");
                    }
                }

                if (!"programada".equals(estado)) {
                    throw new IllegalArgumentException(
                            "La partida no admite inscripciones");
                }

                // Cuenta los inscritos
                int inscritos = 0;

                String sqlContar =
                        "SELECT COUNT(*) FROM inscripciones " +
                                "WHERE id_partida=?";

                try (PreparedStatement p =
                             c.prepareStatement(sqlContar)) {

                    p.setInt(1, inscripcion.getIdPartida());

                    try (ResultSet r = p.executeQuery()) {
                        if (r.next()) {
                            inscritos = r.getInt(1);
                        }
                    }
                }

                if (inscritos >= plazas) {
                    throw new IllegalArgumentException(
                            "No quedan plazas libres");
                }

                // Inserta la inscripcion
                String sqlInsertar =
                        "INSERT INTO inscripciones " +
                                "(id_partida, documento_socio, nombre_invitado) " +
                                "VALUES (?, ?, ?)";

                try (PreparedStatement p =
                             c.prepareStatement(sqlInsertar)) {

                    p.setInt(1, inscripcion.getIdPartida());

                    if (inscripcion.getDocumentoSocio() == null) {
                        p.setNull(2, Types.VARCHAR);
                    } else {
                        p.setString(2,
                                inscripcion.getDocumentoSocio());
                    }

                    if (inscripcion.getNombreInvitado() == null) {
                        p.setNull(3, Types.VARCHAR);
                    } else {
                        p.setString(3,
                                inscripcion.getNombreInvitado());
                    }

                    p.executeUpdate();
                }

                c.commit();

            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    // Quita una inscripcion
    public static boolean quitar(int id) throws SQLException {

        String sql = "DELETE FROM inscripciones WHERE id=?";

        try (Connection c = DatabaseConnection.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    // Marca o desmarca un ganador
    public static boolean marcarGanador(int id, boolean ganador)
            throws SQLException {

        String sql = "UPDATE inscripciones " +
                "SET ganador=? WHERE id=?";

        try (Connection c = DatabaseConnection.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setBoolean(1, ganador);
            p.setInt(2, id);

            return p.executeUpdate() > 0;
        }
    }

    // Muestra los participantes de una partida
    public static String participantes(int idPartida)
            throws SQLException {

        String sql = "SELECT i.id, " +
                "COALESCE(CONCAT(s.nombre, ' ', s.apellidos), " +
                "i.nombre_invitado) AS nombre, " +
                "i.documento_socio, i.ganador " +
                "FROM inscripciones i " +
                "LEFT JOIN socios s " +
                "ON i.documento_socio=s.documento " +
                "WHERE i.id_partida=? ORDER BY i.id";

        StringBuilder texto = new StringBuilder();

        try (Connection c = DatabaseConnection.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, idPartida);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {

                    texto.append("Inscripcion: ")
                            .append(r.getInt("id"));

                    texto.append(" | Nombre: ")
                            .append(r.getString("nombre"));

                    if (r.getString("documento_socio") == null) {
                        texto.append(" | Invitado");
                    } else {
                        texto.append(" | Socio");
                    }

                    if (r.getBoolean("ganador")) {
                        texto.append(" | GANADOR");
                    }

                    texto.append("\n");
                }
            }
        }

        return texto.toString();
    }

    // Busca una inscripcion por ID
    public static Inscripcion buscar(int id) throws SQLException {

        String sql = "SELECT * FROM inscripciones WHERE id=?";

        try (Connection c = DatabaseConnection.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);

            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return convertir(r);
                }
            }
        }

        return null;
    }

    // Consulta las inscripciones de un socio
    public static List<Inscripcion> listarPorSocio(String documento)
            throws SQLException {

        List<Inscripcion> lista = new ArrayList<>();

        String sql = "SELECT * FROM inscripciones " +
                "WHERE documento_socio=? ORDER BY id_partida";

        try (Connection c = DatabaseConnection.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, documento);

            try (ResultSet r = p.executeQuery()) {
                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }
}
