package org.example.dao;

import org.example.conexion.ConexionBD;
import org.example.modelo.*;

import java.sql.*;

public class GestionAsociacion {
    public static Asociacion obtener() throws SQLException {
        String sql = "SELECT * FROM asociacion WHERE id=1";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql);
             ResultSet r = p.executeQuery()) {
            if (!r.next()) return null;
            Date f = r.getDate("fecha_apertura");
            return new Asociacion(r.getInt("id"), r.getString("razon_social"),
                    r.getString("cif"), r.getString("direccion"),
                    r.getString("telefono"), r.getString("email"),
                    f == null ? null : f.toLocalDate());
        }
    }

    public static boolean modificar(Asociacion a) throws SQLException {
        a.validar();
        String sql = "UPDATE asociacion SET razon_social=?,cif=?,direccion=?,telefono=?," +
                "email=?,fecha_apertura=? WHERE id=1";
        try (Connection c = ConexionBD.conectar();
             PreparedStatement p = c.prepareStatement(sql)) {
            p.setString(1, a.getRazonSocial());
            p.setString(2, a.getCif());
            p.setString(3, a.getDireccion());
            p.setString(4, a.getTelefono());
            p.setString(5, a.getEmail());
            if (a.getFechaApertura() == null) p.setNull(6, Types.DATE);
            else p.setDate(6, Date.valueOf(a.getFechaApertura()));
            return p.executeUpdate() > 0;
        }
    }

    // Una sola asociacion. No hace falta un CRUD completo de altas y bajas;
    // en el enunciado se pide poder consultar y modificar sus datos.
}
