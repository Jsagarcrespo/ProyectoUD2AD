package org.example.dao;

import org.example.models.Juego;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionJuegos {

    // Convierte un registro en un objeto Juego
    private static Juego convertir(ResultSet r) throws SQLException {

        Date fecha = r.getDate("fecha_alta");

        return new Juego(r.getInt("id"), r.getString("nombre"), r.getString("autores"), r.getString("descripcion"), r.getInt("min_jugadores"), r.getInt("max_jugadores"), r.getInt("duracion_min"), r.getString("tematica"), r.getString("tipo"), r.getString("ubicacion"), r.getString("estado"), r.getString("documento_propietario"), fecha == null ? null : fecha.toLocalDate());
    }


    // LISTAR TODOS
    public static List<Juego> listar() throws SQLException {

        List<Juego> lista = new ArrayList<>();

        String sql = "SELECT * FROM juegos ORDER BY nombre";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {

            while (r.next()) {
                lista.add(convertir(r));
            }
        }

        return lista;
    }


    // BUSCAR POR ID
    public static Juego buscar(int id) throws SQLException {

        String sql = "SELECT * FROM juegos WHERE id=?";

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


    // AÑADIR
    public static void anadir(Juego juego) throws SQLException {

        juego.validar();

        String sql = "INSERT INTO juegos " + "(nombre, autores, descripcion, min_jugadores, " + "max_jugadores, duracion_min, tematica, tipo, " + "ubicacion, estado, documento_propietario, fecha_alta) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            ponerCampos(p, juego);
            p.executeUpdate();
        }
    }


    // MODIFICAR
    public static boolean modificar(Juego juego) throws SQLException {

        juego.validar();

        String sql = "UPDATE juegos SET nombre=?, autores=?, " + "descripcion=?, min_jugadores=?, max_jugadores=?, " + "duracion_min=?, tematica=?, tipo=?, ubicacion=?, " + "estado=?, documento_propietario=?, fecha_alta=? " + "WHERE id=?";

        try (Connection c = DatabaseConnection.conectar()) {

            // Comprobar que las partidas existentes
            // admiten el nuevo numero de jugadores
            String consulta = "SELECT COUNT(*) FROM partidas " + "WHERE id_juego=? AND estado<>'cancelada' " + "AND (jugadores_deseados<? " + "OR jugadores_deseados>?)";

            try (PreparedStatement comprobacion = c.prepareStatement(consulta)) {

                comprobacion.setInt(1, juego.getId());
                comprobacion.setInt(2, juego.getMinJugadores());
                comprobacion.setInt(3, juego.getMaxJugadores());

                try (ResultSet r = comprobacion.executeQuery()) {

                    if (r.next() && r.getInt(1) > 0) {

                        throw new IllegalArgumentException("El rango de jugadores no sirve " + "para algunas partidas existentes");
                    }
                }
            }

            try (PreparedStatement p = c.prepareStatement(sql)) {

                ponerCampos(p, juego);
                p.setInt(13, juego.getId());

                return p.executeUpdate() > 0;
            }
        }
    }


    // Colocar datos en PreparedStatement
    private static void ponerCampos(PreparedStatement p, Juego juego) throws SQLException {

        p.setString(1, juego.getNombre());
        p.setString(2, juego.getAutores());
        p.setString(3, juego.getDescripcion());
        p.setInt(4, juego.getMinJugadores());
        p.setInt(5, juego.getMaxJugadores());
        p.setInt(6, juego.getDuracionMin());
        p.setString(7, juego.getTematica());
        p.setString(8, juego.getTipo());
        p.setString(9, juego.getUbicacion());
        p.setString(10, juego.getEstado());

        if (juego.getDocumentoPropietario() == null || juego.getDocumentoPropietario().isBlank()) {

            p.setNull(11, Types.VARCHAR);

        } else {

            p.setString(11, juego.getDocumentoPropietario());
        }

        p.setDate(12, Date.valueOf(juego.getFechaAlta()));
    }


    // ELIMINAR
    public static boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM juegos WHERE id=?";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, id);

            return p.executeUpdate() > 0;
        }
    }


    // LISTADO CON PROPIETARIOS
    public static String listadoConPropietarios() throws SQLException {

        String sql = "SELECT j.id, j.nombre, j.estado, " + "CASE WHEN s.documento IS NULL " + "THEN 'Asociacion' " + "ELSE CONCAT(s.nombre, ' ', s.apellidos) " + "END AS propietario " + "FROM juegos j " + "LEFT JOIN socios s " + "ON j.documento_propietario=s.documento " + "ORDER BY j.nombre";

        StringBuilder texto = new StringBuilder();

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {

            while (r.next()) {

                texto.append("ID: ").append(r.getInt("id")).append(" | Juego: ").append(r.getString("nombre")).append(" | Estado: ").append(r.getString("estado")).append(" | Propietario: ").append(r.getString("propietario")).append("\n");
            }
        }

        return texto.toString();
    }


    // BUSCAR POR NOMBRE
    public static List<Juego> buscarPorNombre(String nombre) throws SQLException {

        List<Juego> lista = new ArrayList<>();

        String sql = "SELECT * FROM juegos " + "WHERE nombre LIKE ? ORDER BY nombre";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, "%" + nombre.trim() + "%");

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }


    // NUEVO: BUSCAR POR NOMBRE O AUTOR
    public static List<Juego> buscarPorNombreOAutor(String texto) throws SQLException {

        List<Juego> lista = new ArrayList<>();

        String sql = "SELECT * FROM juegos " + "WHERE nombre LIKE ? OR autores LIKE ? " + "ORDER BY nombre";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            String busqueda = "%" + texto.trim() + "%";

            p.setString(1, busqueda);
            p.setString(2, busqueda);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }


    // NUEVO: BUSCAR POR TEMATICA
    public static List<Juego> buscarPorTematica(String tematica) throws SQLException {

        List<Juego> lista = new ArrayList<>();

        String sql = "SELECT * FROM juegos " + "WHERE tematica LIKE ? ORDER BY nombre";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, "%" + tematica.trim() + "%");

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }


    // NUEVO: BUSCAR POR NUMERO DE JUGADORES
    public static List<Juego> buscarPorNumeroJugadores(int jugadores) throws SQLException {

        List<Juego> lista = new ArrayList<>();

        String sql = "SELECT * FROM juegos " + "WHERE min_jugadores <= ? " + "AND max_jugadores >= ? " + "ORDER BY nombre";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setInt(1, jugadores);
            p.setInt(2, jugadores);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }


    // LISTAR POR ESTADO
    public static List<Juego> listarPorEstado(String estado) throws SQLException {

        List<Juego> lista = new ArrayList<>();

        String sql = "SELECT * FROM juegos " + "WHERE estado=? ORDER BY nombre";

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


    // LISTAR JUEGOS DE UN SOCIO
    public static List<Juego> listarPorPropietario(String documento) throws SQLException {

        List<Juego> lista = new ArrayList<>();

        String sql = "SELECT * FROM juegos " + "WHERE documento_propietario=? ORDER BY nombre";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql)) {

            p.setString(1, documento);

            try (ResultSet r = p.executeQuery()) {

                while (r.next()) {
                    lista.add(convertir(r));
                }
            }
        }

        return lista;
    }


    // LISTAR JUEGOS DE LA ASOCIACION
    public static List<Juego> listarDeLaAsociacion() throws SQLException {

        List<Juego> lista = new ArrayList<>();

        String sql = "SELECT * FROM juegos " + "WHERE documento_propietario IS NULL " + "ORDER BY nombre";

        try (Connection c = DatabaseConnection.conectar(); PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {

            while (r.next()) {
                lista.add(convertir(r));
            }
        }

        return lista;
    }
}
