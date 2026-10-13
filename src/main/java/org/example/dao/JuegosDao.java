package org.example.dao;

import org.example.conexion.ConexionBD;
import org.example.modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionJuegos {
    private static Juego convertir(ResultSet r) throws SQLException {
        Date f = r.getDate("fecha_alta");
        return new Juego(r.getInt("id"), r.getString("nombre"), r.getString("autores"),
                r.getString("descripcion"), r.getInt("min_jugadores"),
                r.getInt("max_jugadores"), r.getInt("duracion_min"),
                r.getString("tematica"), r.getString("tipo"), r.getString("ubicacion"),
                r.getString("estado"), r.getString("documento_propietaria"),
                f == null ? null : f.toLocalDate());
    }

    public static List<Juego> listar() throws SQLException {
        List<Juego> lista = new ArrayList<>();
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement("SELECT * FROM juegos ORDER BY nombre");
             ResultSet r = p.executeQuery()) {
            while (r.next()) lista.add(convertir(r));
        }
        return lista;
    }

    public static Juego buscar(int id) throws SQLException {
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement("SELECT * FROM juegos WHERE id=?")) {
            p.setInt(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? convertir(r) : null;
            }
        }
    }

    public static void anadir(Juego j) throws SQLException {
        j.validar();
        String sql = "INSERT INTO juegos(nombre,autores,descripcion,min_jugadores,max_jugadores," +
                "duracion_min,tematica,tipo,ubicacion,estado,documento_propietaria,fecha_alta) " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            ponerCampos(p, j);
            p.executeUpdate();
        }
    }

    public static boolean modificar(Juego j) throws SQLException {
        j.validar();
        String sql = "UPDATE juegos SET nombre=?,autores=?,descripcion=?,min_jugadores=?," +
                "max_jugadores=?,duracion_min=?,tematica=?,tipo=?,ubicacion=?,estado=?," +
                "documento_propietaria=?,fecha_alta=? WHERE id=?";
        try (Connection c = ConexionBD.conectar()) {
            // No reducir el rango si deja fuera a partidas ya programadas.
            try (PreparedStatement comprobacion = c.prepareStatement(
                    "SELECT COUNT(*) FROM partidas WHERE id_juego=? AND estado<>'cancelada' " +
                    "AND (jugadores_deseados<? OR jugadores_deseados>?)")) {
                comprobacion.setInt(1, j.getId());
                comprobacion.setInt(2, j.getMinJugadores());
                comprobacion.setInt(3, j.getMaxJugadores());
                try (ResultSet resultado = comprobacion.executeQuery()) {
                    resultado.next();
                    if (resultado.getInt(1) > 0)
                        throw new IllegalArgumentException("El nuevo rango no sirve para algunas partidas existentes.");
                }
            }
            try (PreparedStatement p = c.prepareStatement(sql)) {
                ponerCampos(p, j);
                p.setInt(13, j.getId());
                return p.executeUpdate() > 0;
            }
        }
    }

    private static void ponerCampos(PreparedStatement p, Juego j) throws SQLException {
        p.setString(1, j.getNombre());
        p.setString(2, j.getAutores());
        p.setString(3, j.getDescripcion());
        p.setInt(4, j.getMinJugadores());
        p.setInt(5, j.getMaxJugadores());
        p.setInt(6, j.getDuracionMin());
        p.setString(7, j.getTematica());
        p.setString(8, j.getTipo());
        p.setString(9, j.getUbicacion());
        p.setString(10, j.getEstado());
        if (j.getDocumentoPropietaria() == null || j.getDocumentoPropietaria().isBlank())
            p.setNull(11, Types.VARCHAR);
        else
            p.setString(11, j.getDocumentoPropietaria());
        p.setDate(12, Date.valueOf(j.getFechaAlta()));
    }

    public static boolean eliminar(int id) throws SQLException {
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement("DELETE FROM juegos WHERE id=?")) {
            p.setInt(1, id);
            return p.executeUpdate() > 0;
        }
    }

    public static String listadoConPropietarias() throws SQLException {
        String sql = "SELECT j.id, j.nombre, j.estado, CASE WHEN s.documento IS NULL " +
                "THEN 'Asociacion' ELSE CONCAT(s.nombre,' ',s.apellidos) END AS propietario " +
                "FROM juegos j LEFT JOIN socias s ON j.documento_propietaria=s.documento ORDER BY j.nombre";
        StringBuilder b = new StringBuilder();
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) b.append(r.getInt(1)).append(" | ").append(r.getString(2))
                    .append(" | ").append(r.getString(3)).append(" | ")
                    .append(r.getString(4)).append("\n");
        }
        return b.toString();
    }

    // Buscador parcial por nombre del juego.
    public static List<Juego> buscarPorNombre(String texto) throws SQLException {
        List<Juego> lista = new ArrayList<>();
        String sql = "SELECT * FROM juegos WHERE nombre LIKE ? ORDER BY nombre";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, "%" + (texto == null ? "" : texto.trim()) + "%");
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) lista.add(convertir(r));
            }
        }
        return lista;
    }

    // Estados validos: disponible, prestado y reparacion.
    public static List<Juego> listarPorEstado(String estado) throws SQLException {
        List<Juego> lista = new ArrayList<>();
        String sql = "SELECT * FROM juegos WHERE estado = ? ORDER BY nombre";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, estado);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) lista.add(convertir(r));
            }
        }
        return lista;
    }

    // Juegos que pertenecen a una socia determinada.
    public static List<Juego> listarPorPropietaria(String documento) throws SQLException {
        List<Juego> lista = new ArrayList<>();
        String sql = "SELECT * FROM juegos WHERE documento_propietaria = ? ORDER BY nombre";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, documento);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) lista.add(convertir(r));
            }
        }
        return lista;
    }

    // Juegos propiedad de la propia asociacion: propietaria NULL.
    public static List<Juego> listarDeLaAsociacion() throws SQLException {
        List<Juego> lista = new ArrayList<>();
        String sql = "SELECT * FROM juegos WHERE documento_propietaria IS NULL ORDER BY nombre";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) lista.add(convertir(r));
        }
        return lista;
    }
}
