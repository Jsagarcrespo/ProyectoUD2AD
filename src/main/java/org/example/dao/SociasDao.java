package org.example.dao;

import org.example.conexion.ConexionBD;
import org.example.modelo.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionSocias {
    private static Socia convertir(ResultSet r) throws SQLException {
        return new Socia(r.getString("documento"), r.getString("nombre"),
                r.getString("apellidos"), r.getString("telefono"),
                r.getInt("edad"), r.getString("email"));
    }

    public static List<Socia> listar() throws SQLException {
        List<Socia> lista = new ArrayList<>();
        String sql = "SELECT * FROM socias ORDER BY apellidos, nombre";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            while (r.next()) lista.add(convertir(r));
        }
        return lista;
    }

    public static Socia buscar(String documento) throws SQLException {
        String sql = "SELECT * FROM socias WHERE documento=?";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, documento);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? convertir(r) : null;
            }
        }
    }

    public static void anadir(Socia s) throws SQLException {
        s.validar();
        String sql = "INSERT INTO socias(documento,nombre,apellidos,telefono,edad,email) "
                   + "VALUES(?,?,?,?,?,?)";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            ponerCampos(p, s, false);
            p.executeUpdate();
        }
    }

    public static boolean modificar(Socia s) throws SQLException {
        s.validar();
        String sql = "UPDATE socias SET nombre=?,apellidos=?,telefono=?,edad=?,email=? "
                   + "WHERE documento=?";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            ponerCampos(p, s, true);
            return p.executeUpdate() > 0;
        }
    }

    private static void ponerCampos(PreparedStatement p, Socia s, boolean modificar) throws SQLException {
        if (!modificar) p.setString(1, s.getDocumento());
        int x = modificar ? 1 : 2;
        p.setString(x++, s.getNombre());
        p.setString(x++, s.getApellidos());
        p.setString(x++, s.getTelefono());
        p.setInt(x++, s.getEdad());
        p.setString(x++, s.getEmail());
        if (modificar) p.setString(x, s.getDocumento());
    }

    // Borrado físico: la base de datos impide borrar socias que tengan juegos o inscripciones.
    public static boolean eliminar(String documento) throws SQLException {
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement("DELETE FROM socias WHERE documento=?")) {
            p.setString(1, documento);
            return p.executeUpdate() > 0;
        }
    }

    public static String historial(String documento) throws SQLException {
        StringBuilder b = new StringBuilder("JUEGOS EN PROPIEDAD:\n");
        try (Connection c = ConexionBD.conectar()) {
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT id,nombre,estado FROM juegos WHERE documento_propietaria=? ORDER BY nombre")) {
                p.setString(1, documento);
                try (ResultSet r = p.executeQuery()) {
                    while (r.next()) b.append(r.getInt("id")).append(" - ")
                            .append(r.getString("nombre")).append(" (")
                            .append(r.getString("estado")).append(")\n");
                }
            }
            b.append("\nPARTIDAS EN LAS QUE PARTICIPA:\n");
            try (PreparedStatement p = c.prepareStatement(
                    "SELECT p.id,j.nombre,p.fecha,p.hora,i.ganadora " +
                    "FROM inscripciones i JOIN partidas p ON i.id_partida=p.id " +
                    "JOIN juegos j ON p.id_juego=j.id WHERE i.documento_socia=? " +
                    "ORDER BY p.fecha DESC,p.hora DESC")) {
                p.setString(1, documento);
                try (ResultSet r = p.executeQuery()) {
                    while (r.next()) b.append("Partida ").append(r.getInt(1)).append(" - ")
                            .append(r.getString(2)).append(" - ").append(r.getDate(3))
                            .append(" ").append(r.getTime(4))
                            .append(r.getBoolean(5) ? " - Ganadora" : "").append("\n");
                }
            }
        }
        return b.toString();
    }

    // Buscar socias por nombre o apellidos (busqueda parcial).
    public static List<Socia> buscarPorNombre(String texto) throws SQLException {
        List<Socia> lista = new ArrayList<>();
        String sql = "SELECT * FROM socias WHERE nombre LIKE ? OR apellidos LIKE ? " +
                "ORDER BY apellidos, nombre";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            String filtro = "%" + (texto == null ? "" : texto.trim()) + "%";
            p.setString(1, filtro);
            p.setString(2, filtro);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) lista.add(convertir(r));
            }
        }
        return lista;
    }

    // Listar las socias apuntadas a una partida.
    public static List<Socia> listarPorPartida(int idPartida) throws SQLException {
        List<Socia> lista = new ArrayList<>();
        String sql = "SELECT s.* FROM socias s " +
                "JOIN inscripciones i ON i.documento_socia = s.documento " +
                "WHERE i.id_partida = ? ORDER BY s.apellidos, s.nombre";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setInt(1, idPartida);
            try (ResultSet r = p.executeQuery()) {
                while (r.next()) lista.add(convertir(r));
            }
        }
        return lista;
    }
}
