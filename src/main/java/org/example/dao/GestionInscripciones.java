package org.example.dao;

import org.example.models.Inscripcion;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionInscripciones {


    // Obtiene las inscripciones de una partida
    public static List<Inscripcion> listar(int idPartida) throws SQLException {

        List<Inscripcion> inscripciones = new ArrayList<>();

        String sql = "SELECT * FROM inscripciones " + "WHERE id_partida=? ORDER BY id";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPartida);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    Inscripcion inscripcion = new Inscripcion(resultado.getInt("id"), resultado.getInt("id_partida"), resultado.getString("documento_socia"), resultado.getString("nombre_invitada"), resultado.getBoolean("ganadora"));

                    inscripciones.add(inscripcion);
                }
            }
        }

        return inscripciones;
    }


    // Inscribe una socia en una partida
    public static void inscribirSocia(int idPartida, String documento) throws SQLException {

        Inscripcion inscripcion = new Inscripcion(0, idPartida, documento, null, false);

        inscribir(inscripcion);
    }


    // Inscribe una persona invitada en una partida
    public static void inscribirInvitada(int idPartida, String nombre) throws SQLException {

        Inscripcion inscripcion = new Inscripcion(0, idPartida, null, nombre, false);

        inscribir(inscripcion);
    }


    // Guarda una inscripcion comprobando que haya plazas
    public static void inscribir(Inscripcion inscripcion) throws SQLException {

        inscripcion.validar();

        try (Connection conexion = DatabaseConnection.conectar()) {

            conexion.setAutoCommit(false);

            try {

                int plazas;
                String estado;

                // Busca la partida y comprueba su estado
                String sqlPartida = "SELECT jugadores_deseados, estado " + "FROM partidas WHERE id=? FOR UPDATE";

                try (PreparedStatement sentencia = conexion.prepareStatement(sqlPartida)) {

                    sentencia.setInt(1, inscripcion.getIdPartida());

                    try (ResultSet resultado = sentencia.executeQuery()) {

                        if (!resultado.next()) {
                            throw new IllegalArgumentException("La partida no existe");
                        }

                        plazas = resultado.getInt("jugadores_deseados");
                        estado = resultado.getString("estado");
                    }
                }


                // Solo se puede inscribir en partidas programadas
                if (!estado.equals("programada")) {

                    throw new IllegalArgumentException("La partida no admite inscripciones");
                }


                // Cuenta las personas inscritas
                int inscritos = 0;

                String sqlContar = "SELECT COUNT(*) FROM inscripciones " + "WHERE id_partida=?";

                try (PreparedStatement sentencia = conexion.prepareStatement(sqlContar)) {

                    sentencia.setInt(1, inscripcion.getIdPartida());

                    try (ResultSet resultado = sentencia.executeQuery()) {

                        if (resultado.next()) {
                            inscritos = resultado.getInt(1);
                        }
                    }
                }


                // Comprueba si quedan plazas libres
                if (inscritos >= plazas) {

                    throw new IllegalArgumentException("No quedan plazas libres en esta partida");
                }


                // Guarda la nueva inscripcion
                String sqlInsertar = "INSERT INTO inscripciones " + "(id_partida, documento_socia, nombre_invitada) " + "VALUES (?, ?, ?)";

                try (PreparedStatement sentencia = conexion.prepareStatement(sqlInsertar)) {

                    sentencia.setInt(1, inscripcion.getIdPartida());

                    if (inscripcion.getDocumentoSocia() == null) {

                        sentencia.setNull(2, Types.VARCHAR);

                    } else {

                        sentencia.setString(2, inscripcion.getDocumentoSocia());
                    }

                    if (inscripcion.getNombreInvitada() == null) {

                        sentencia.setNull(3, Types.VARCHAR);

                    } else {

                        sentencia.setString(3, inscripcion.getNombreInvitada());
                    }

                    sentencia.executeUpdate();
                }

                conexion.commit();

            } catch (SQLException | RuntimeException e) {

                conexion.rollback();
                throw e;
            }
        }
    }


    // Elimina una inscripcion mediante su ID
    public static boolean quitar(int id) throws SQLException {

        String sql = "DELETE FROM inscripciones WHERE id=?";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            return sentencia.executeUpdate() > 0;
        }
    }


    // Marca o desmarca una inscripcion como ganadora
    public static boolean marcarGanadora(int id, boolean ganadora) throws SQLException {

        String sql = "UPDATE inscripciones SET ganadora=? WHERE id=?";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setBoolean(1, ganadora);
            sentencia.setInt(2, id);

            return sentencia.executeUpdate() > 0;
        }
    }


    // Muestra los participantes de una partida
    // indicando si son socias o invitadas
    public static String participantes(int idPartida) throws SQLException {

        String sql = "SELECT i.id, " + "COALESCE(CONCAT(s.nombre, ' ', s.apellidos), " + "i.nombre_invitada) AS nombre, " + "i.documento_socia, i.ganadora " + "FROM inscripciones i " + "LEFT JOIN socias s " + "ON i.documento_socia=s.documento " + "WHERE i.id_partida=? ORDER BY i.id";

        StringBuilder texto = new StringBuilder();

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPartida);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    texto.append("Inscripcion: ").append(resultado.getInt("id"));

                    texto.append(" | Nombre: ").append(resultado.getString("nombre"));

                    if (resultado.getString("documento_socia") == null) {

                        texto.append(" | Invitada");

                    } else {

                        texto.append(" | Socia");
                    }

                    if (resultado.getBoolean("ganadora")) {

                        texto.append(" | GANADORA");
                    }

                    texto.append("\n");
                }
            }
        }

        return texto.toString();
    }


    // Busca una inscripcion mediante su ID
    public static Inscripcion buscar(int id) throws SQLException {

        String sql = "SELECT * FROM inscripciones WHERE id=?";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    return new Inscripcion(resultado.getInt("id"), resultado.getInt("id_partida"), resultado.getString("documento_socia"), resultado.getString("nombre_invitada"), resultado.getBoolean("ganadora"));
                }
            }
        }

        return null;
    }


    // Obtiene las partidas en las que esta inscrita una socia
    public static List<Inscripcion> listarPorSocia(String documento) throws SQLException {

        List<Inscripcion> inscripciones = new ArrayList<>();

        String sql = "SELECT * FROM inscripciones " + "WHERE documento_socia=? ORDER BY id_partida";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, documento);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    Inscripcion inscripcion = new Inscripcion(resultado.getInt("id"), resultado.getInt("id_partida"), resultado.getString("documento_socia"), resultado.getString("nombre_invitada"), resultado.getBoolean("ganadora"));

                    inscripciones.add(inscripcion);
                }
            }
        }

        return inscripciones;
    }
}
