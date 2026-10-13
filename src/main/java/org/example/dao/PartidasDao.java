package org.example.dao;

import org.example.conexion.ConexionBD;
import org.example.modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionPartidas {
    private static Partida convertir(ResultSet r) throws SQLException {
        return new Partida(r.getInt("id"), r.getInt("id_juego"),
                r.getDate("fecha").toLocalDate(), r.getTime("hora").toLocalTime(),
                r.getInt("jugadores_deseados"), r.getString("estado"));
    }

    public static List<Partida> listar() throws SQLException {
        List<Partida> lista = new ArrayList<>();
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement("SELECT * FROM partidas ORDER BY fecha, hora");
             ResultSet r = p.executeQuery()) {
            while (r.next()) lista.add(convertir(r));
        }
        return lista;
    }

    public static Partida buscar(int id) throws SQLException {
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement("SELECT * FROM partidas WHERE id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? convertir(r) : null;
            }
        }
    }

    private static int[] rangoJuego(Connection c, int juego) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT min_jugadores,max_jugadores FROM juegos WHERE id=?")) {
            p.setInt(1, juego);
            try (ResultSet r = p.executeQuery()) {
                if (!r.next()) throw new IllegalArgumentException("El juego no existe.");
                return new int[]{r.getInt(1), r.getInt(2)};
            }
        }
    }

    public static void anadir(Partida partida) throws SQLException {
        try (Connection c = ConexionBD.conectar()) {
            int[] rango = rangoJuego(c, partida.getIdJuego());
            partida.validar(rango[0], rango[1]);
            String sql = "INSERT INTO partidas(id_juego,fecha,hora,jugadores_deseados,estado) " +
                    "VALUES(?,?,?,?,?)";
            try (PreparedStatement p = c.prepareStatement(sql)) {
                ponerCampos(p, partida);
                p.executeUpdate();
            }
        }
    }

    public static boolean modificar(Partida partida) throws SQLException {
        try (Connection c = ConexionBD.conectar()) {
            c.setAutoCommit(false);
            try {
                int juegoActual;
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT id_juego FROM partidas WHERE id=? FOR UPDATE")) {
                    p.setInt(1, partida.getId());
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next()) { c.rollback(); return false; }
                        juegoActual = r.getInt(1);
                    }
                }
                int inscritos = inscritos(c, partida.getId());
                if (inscritos > 0 && juegoActual != partida.getIdJuego())
                    throw new IllegalArgumentException("No se puede cambiar el juego si hay participantes inscritos.");
                int[] rango = rangoJuego(c, partida.getIdJuego());
                partida.validar(rango[0], rango[1]);
                if (inscritos > partida.getJugadoresDeseados())
                    throw new IllegalArgumentException("La partida ya tiene " + inscritos + " participantes.");
                String sql = "UPDATE partidas SET id_juego=?,fecha=?,hora=?,jugadores_deseados=?,estado=? WHERE id=?";
                int filas;
                try (PreparedStatement p = c.prepareStatement(sql)) {
                    ponerCampos(p, partida);
                    p.setInt(6, partida.getId());
                    filas = p.executeUpdate();
                }
                c.commit();
                return filas > 0;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        }
    }

    private static void ponerCampos(PreparedStatement p, Partida partida) throws SQLException {
        p.setInt(1, partida.getIdJuego());
        p.setDate(2, Date.valueOf(partida.getFecha()));
        p.setTime(3, Time.valueOf(partida.getHora()));
        p.setInt(4, partida.getJugadoresDeseados());
        p.setString(5, partida.getEstado());
    }

    public static boolean cancelar(int id) throws SQLException {
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(
                     "UPDATE partidas SET estado='cancelada' WHERE id=?")) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    public static boolean eliminar(int id) throws SQLException {
        // Si hay inscripciones, MariaDB impide eliminar la partida. Usa 'cancelar'.
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement("DELETE FROM partidas WHERE id=?")) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    private static int inscritos(Connection c, int id) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(
                "SELECT COUNT(*) FROM inscripciones WHERE id_partida=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                r.next();
                return r.getInt(1);
            }
        }
    }

    public static int plazasLibres(int id) throws SQLException {
        try (Connection c = ConexionBD.conectar()) {
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT jugadores_deseados FROM partidas WHERE id=?")) {
                p.setInt(1, id);
                try (ResultSet r = p.executeQuery()) {
                    if (!r.next()) throw new IllegalArgumentException("Partida no encontrada.");
                    return Partida.plazasLibres(r.getInt(1), inscritos(c, id));
                }
            }
        }
    }

    public static String listadoConPlazas() throws SQLException {
        String sql = "SELECT p.id,j.nombre,p.fecha,p.hora,p.jugadores_deseados,p.estado," +
                "COUNT(i.id) AS inscritos FROM partidas p JOIN juegos j ON p.id_juego=j.id " +
                "LEFT JOIN inscripciones i ON p.id=i.id_partida " +
                "GROUP BY p.id,j.nombre,p.fecha,p.hora,p.jugadores_deseados,p.estado " +
                "ORDER BY p.fecha,p.hora";
        StringBuilder s = new StringBuilder();
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) s.append("#").append(r.getInt(1)).append(" | ")
                    .append(r.getString(2)).append(" | ").append(r.getDate(3))
                    .append(" ").append(r.getTime(4)).append(" | ")
                    .append(r.getInt(7)).append("/").append(r.getInt(5))
                    .append(" | libres: ").append(r.getInt(5) - r.getInt(7))
                    .append(" | ").append(r.getString(6)).append("\n");
        }
        return s.toString();
    }

    // Obtener partidas que utilizan un juego concreto.
    public static List<Partida> listarPorJuego(int idJuego) throws SQLException {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT * FROM partidas WHERE id_juego = ? ORDER BY fecha, hora";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, idJuego);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) lista.add(convertir(r));
            }
        }
        return lista;
    }

    public static List<Partida> listarPorFecha(java.time.LocalDate fecha) throws SQLException {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT * FROM partidas WHERE fecha = ? ORDER BY hora";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setDate(1, Date.valueOf(fecha));
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) lista.add(convertir(r));
            }
        }
        return lista;
    }

    // Estados validos: programada, finalizada y cancelada.
    public static List<Partida> listarPorEstado(String estado) throws SQLException {
        List<Partida> lista = new ArrayList<>();
        String sql = "SELECT * FROM partidas WHERE estado = ? ORDER BY fecha, hora";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, estado);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) lista.add(convertir(r));
            }
        }
        return lista;
    }

    // No permite finalizar partidas canceladas o que ya esten finalizadas.
    public static boolean finalizar(int id) throws SQLException {
        String sql = "UPDATE partidas SET estado = 'finalizada' " +
                "WHERE id = ? AND estado = 'programada'";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }
}
