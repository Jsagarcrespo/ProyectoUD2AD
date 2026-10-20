package org.example.dao;

import org.example.utils.DatabaseConnection;
import org.example.models.Partida;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class GestionPartidas {

    // Convierte un registro de MariaDB en una Partida
    private static Partida convertir(ResultSet r) throws SQLException {

        return new Partida(r.getInt("id"), r.getInt("id_juego"), r.getDate("fecha").toLocalDate(), r.getTime("hora").toLocalTime(), r.getInt("jugadores_deseados"), r.getString("estado"));
    }


    // LISTAR TODAS LAS PARTIDAS
    public static List<Partida> listar() throws SQLException {

        List<Partida> lista = new ArrayList<>();

        String sql = "SELECT * FROM partidas ORDER BY fecha, hora";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {

            while (r.next()) {
                lista.add(convertir(r));
            }
        }

        return lista;
    }


    // BUSCAR PARTIDA POR ID
    public static Partida buscar(int id) throws SQLException {

        String sql = "SELECT * FROM partidas WHERE id=?";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);

            try (ResultSet r = p.executeQuery()) {

                if (r.next()) {
                    return convertir(r);
                }
            }
        }

        return null;
    }


    // OBTENER EL RANGO DE JUGADORES DE UN JUEGO
    private static int[] rangoJuego(Connection c, int juego) throws SQLException {

        String sql = "SELECT min_jugadores, max_jugadores " + "FROM juegos WHERE id=?";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, juego);

            try (ResultSet r = p.executeQuery()) {

                if (!r.next()) {
                    throw new IllegalArgumentException("El juego no existe.");
                }

                return new int[]{r.getInt(1), r.getInt(2)};
            }
        }
    }


    // AÑADIR PARTIDA
    public static void anadir(Partida partida) throws SQLException {

        try (Connection c = DatabaseConnection.conectar()) {

            int[] rango = rangoJuego(c, partida.getIdJuego());

            partida.validar(rango[0], rango[1]);

            String sql = "INSERT INTO partidas " + "(id_juego, fecha, hora, " + "jugadores_deseados, estado) " + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement p = c.prepareStatement(sql)) {

                ponerCampos(p, partida);
                p.executeUpdate();
            }
        }
    }


    // MODIFICAR PARTIDA
    public static boolean modificar(Partida partida) throws SQLException {

        try (Connection c = DatabaseConnection.conectar()) {

            c.setAutoCommit(false);

            try {

                int juegoActual;

                String consulta = "SELECT id_juego FROM partidas " + "WHERE id=? FOR UPDATE";

                try (PreparedStatement p = c.prepareStatement(consulta)) {

                    p.setInt(1, partida.getId());

                    try (ResultSet r = p.executeQuery()) {

                        if (!r.next()) {

                            c.rollback();
                            return false;
                        }

                        juegoActual = r.getInt(1);
                    }
                }

                int inscritos = inscritos(c, partida.getId());

                // No cambiar el juego si hay inscritos
                if (inscritos > 0 && juegoActual != partida.getIdJuego()) {

                    throw new IllegalArgumentException("No se puede cambiar el juego " + "si hay participantes inscritos.");
                }

                int[] rango = rangoJuego(c, partida.getIdJuego());

                partida.validar(rango[0], rango[1]);

                if (inscritos > partida.getJugadoresDeseados()) {

                    throw new IllegalArgumentException("La partida ya tiene " + inscritos + " participantes.");
                }

                String sql = "UPDATE partidas SET " + "id_juego=?, fecha=?, hora=?, " + "jugadores_deseados=?, estado=? " + "WHERE id=?";

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


    // COLOCAR DATOS EN EL PREPAREDSTATEMENT
    private static void ponerCampos(PreparedStatement p, Partida partida) throws SQLException {

        p.setInt(1, partida.getIdJuego());
        p.setDate(2, Date.valueOf(partida.getFecha()));
        p.setTime(3, Time.valueOf(partida.getHora()));
        p.setInt(4, partida.getJugadoresDeseados());
        p.setString(5, partida.getEstado());
    }


    // CANCELAR PARTIDA
    public static boolean cancelar(int id) throws SQLException {

        String sql = "UPDATE partidas SET estado='cancelada' " + "WHERE id=?";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);

            return p.executeUpdate() > 0;
        }
    }


    // ELIMINAR PARTIDA
    public static boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM partidas WHERE id=?";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);

            return p.executeUpdate() > 0;
        }
    }


    // CONTAR LOS INSCRITOS EN UNA PARTIDA
    private static int inscritos(Connection c, int id) throws SQLException {

        String sql = "SELECT COUNT(*) FROM inscripciones " + "WHERE id_partida=?";

        try (PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);

            try (ResultSet r = p.executeQuery()) {

                r.next();
                return r.getInt(1);
            }
        }
    }


    // CALCULAR PLAZAS LIBRES
    public static int plazasLibres(int id) throws SQLException {

        try (Connection c = DatabaseConnection.conectar()) {

            String sql = "SELECT jugadores_deseados " + "FROM partidas WHERE id=?";

            try (PreparedStatement p = c.prepareStatement(sql)) {

                p.setInt(1, id);

                try (ResultSet r = p.executeQuery()) {

                    if (!r.next()) {

                        throw new IllegalArgumentException("Partida no encontrada.");
                    }

                    int jugadores = r.getInt(1);
                    int ocupadas = inscritos(c, id);

                    return Partida.plazasLibres(jugadores, ocupadas);
                }
            }
        }
    }


    // LISTADO DE PARTIDAS CON PLAZAS
    public static String listadoConPlazas() throws SQLException {

        String sql = "SELECT p.id, j.nombre, p.fecha, p.hora, " + "p.jugadores_deseados, p.estado, " + "COUNT(i.id) AS inscritos " + "FROM partidas p " + "JOIN juegos j ON p.id_juego=j.id " + "LEFT JOIN inscripciones i " + "ON p.id=i.id_partida " + "GROUP BY p.id, j.nombre, p.fecha, " + "p.hora, p.jugadores_deseados, p.estado " + "ORDER BY p.fecha, p.hora";

        StringBuilder texto = new StringBuilder();

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {

            while (r.next()) {

                texto.append("#").append(r.getInt(1));

                texto.append(" | ").append(r.getString(2));

                texto.append(" | ").append(r.getDate(3));

                texto.append(" ").append(r.getTime(4));

                texto.append(" | ").append(r.getInt(7)).append("/").append(r.getInt(5));

                texto.append(" | libres: ").append(r.getInt(5) - r.getInt(7));

                texto.append(" | ").append(r.getString(6)).append("\n");
            }
        }

        return texto.toString();
    }


    // LISTAR PARTIDAS DE UN JUEGO
    public static List<Partida> listarPorJuego(int idJuego) throws SQLException {

        List<Partida> lista = new ArrayList<>();

        String sql = "SELECT * FROM partidas " + "WHERE id_juego=? ORDER BY fecha, hora";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, idJuego);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }


    // LISTAR PARTIDAS POR FECHA
    public static List<Partida> listarPorFecha(LocalDate fecha) throws SQLException {

        List<Partida> lista = new ArrayList<>();

        String sql = "SELECT * FROM partidas " + "WHERE fecha=? ORDER BY hora";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setDate(1, Date.valueOf(fecha));

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }


    // LISTAR PARTIDAS POR ESTADO
    public static List<Partida> listarPorEstado(String estado) throws SQLException {

        List<Partida> lista = new ArrayList<>();

        String sql = "SELECT * FROM partidas " + "WHERE estado=? ORDER BY fecha, hora";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, estado);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }


    // FINALIZAR PARTIDA
    public static boolean finalizar(int id) throws SQLException {

        String sql = "UPDATE partidas SET estado='finalizada' " + "WHERE id=? AND estado='programada'";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);

            return p.executeUpdate() > 0;
        }
    }


    // NUEVO: BUSQUEDA AVANZADA DE PARTIDAS
    public static List<Partida> buscarAvanzado(int opcion, String busqueda) throws SQLException {

        List<Partida> lista = new ArrayList<>();

        String condicion;

        // Elegir el criterio de busqueda
        if (opcion == 0) {

            // ID de la partida
            condicion = "p.id=?";

        } else if (opcion == 1) {

            // Nombre del juego
            condicion = "j.nombre LIKE ?";

        } else if (opcion == 2) {

            // ID del juego
            condicion = "p.id_juego=?";

        } else if (opcion == 3) {

            // Fecha
            condicion = "p.fecha=?";

        } else if (opcion == 4) {

            // Estado
            condicion = "p.estado LIKE ?";

        } else {

            throw new IllegalArgumentException("Tipo de busqueda incorrecto");
        }

        // JOIN para poder buscar por nombre del juego
        String sql = "SELECT p.* FROM partidas p " + "JOIN juegos j ON p.id_juego=j.id " + "WHERE " + condicion + " ORDER BY p.fecha, p.hora";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            if (opcion == 0 || opcion == 2) {

                // Buscar por ID
                int numero = Integer.parseInt(busqueda.trim());

                if (numero <= 0) {
                    throw new IllegalArgumentException("El ID debe ser mayor que cero");
                }

                p.setInt(1, numero);

            } else if (opcion == 3) {

                // Buscar por fecha
                LocalDate fecha = LocalDate.parse(busqueda.trim());

                p.setDate(1, Date.valueOf(fecha));

            } else {

                // Nombre de juego o estado
                p.setString(1, "%" + busqueda.trim() + "%");
            }

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }
}
