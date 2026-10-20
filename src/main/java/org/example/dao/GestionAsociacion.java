package org.example.dao;

import org.example.models.Asociacion;
import org.example.utils.DatabaseConnection;

import java.sql.*;

public class GestionAsociacion {

    // Obtiene los datos de la asociacion
    public static Asociacion obtener() throws SQLException {

        String sql = "SELECT * FROM asociacion WHERE id = 1";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql); ResultSet resultado = sentencia.executeQuery()) {

            if (resultado.next()) {

                Date fecha = resultado.getDate("fecha_apertura");

                return new Asociacion(resultado.getInt("id"), resultado.getString("razon_social"), resultado.getString("cif"), resultado.getString("direccion"), resultado.getString("telefono"), resultado.getString("email"), fecha == null ? null : fecha.toLocalDate());
            }
        }

        return null;
    }

    // Modifica los datos de la asociacion
    public static boolean modificar(Asociacion asociacion) throws SQLException {

        asociacion.validar();

        String sql = "UPDATE asociacion SET razon_social=?, cif=?, direccion=?, telefono=?, email=?, fecha_apertura=? WHERE id=1";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, asociacion.getRazonSocial());
            sentencia.setString(2, asociacion.getCif());
            sentencia.setString(3, asociacion.getDireccion());
            sentencia.setString(4, asociacion.getTelefono());
            sentencia.setString(5, asociacion.getEmail());

            if (asociacion.getFechaApertura() == null) {
                sentencia.setNull(6, Types.DATE);
            } else {
                sentencia.setDate(6, Date.valueOf(asociacion.getFechaApertura()));
            }

            return sentencia.executeUpdate() > 0;
        }
    }
}
