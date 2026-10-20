package org.example.dao;

import org.example.models.Socio;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionSocios {

    // Convierte un registro de MariaDB en un objeto Socio
    private static Socio convertir(ResultSet resultado) throws SQLException {

        return new Socio(resultado.getString("documento"), resultado.getString("nombre"), resultado.getString("apellidos"), resultado.getString("telefono"), resultado.getInt("edad"), resultado.getString("email"));
    }


    // MUESTRA TODOS LOS SOCIOS
    public static List<Socio> listar() throws SQLException {

        List<Socio> socios = new ArrayList<>();

        String sql = "SELECT * FROM socios ORDER BY apellidos, nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql); ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {
                socios.add(convertir(resultado));
            }
        }

        return socios;
    }


    // BUSCA POR DOCUMENTO EXACTO
    public static Socio buscar(String documento) throws SQLException {

        String sql = "SELECT * FROM socios WHERE documento=?";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, documento);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {
                    return convertir(resultado);
                }
            }
        }

        return null;
    }


    // AÑADE UN SOCIO
    public static void anadir(Socio socio) throws SQLException {

        socio.validar();

        String sql = "INSERT INTO socios " + "(documento, nombre, apellidos, telefono, edad, email) " + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, socio.getDocumento());
            sentencia.setString(2, socio.getNombre());
            sentencia.setString(3, socio.getApellidos());
            sentencia.setString(4, socio.getTelefono());
            sentencia.setInt(5, socio.getEdad());
            sentencia.setString(6, socio.getEmail());

            sentencia.executeUpdate();
        }
    }


    // MODIFICA UN SOCIO
    public static boolean modificar(Socio socio) throws SQLException {

        socio.validar();

        String sql = "UPDATE socios SET nombre=?, apellidos=?, " + "telefono=?, edad=?, email=? WHERE documento=?";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, socio.getNombre());
            sentencia.setString(2, socio.getApellidos());
            sentencia.setString(3, socio.getTelefono());
            sentencia.setInt(4, socio.getEdad());
            sentencia.setString(5, socio.getEmail());
            sentencia.setString(6, socio.getDocumento());

            return sentencia.executeUpdate() > 0;
        }
    }


    // ELIMINA UN SOCIO
    public static boolean eliminar(String documento) throws SQLException {

        String sql = "DELETE FROM socios WHERE documento=?";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, documento);

            return sentencia.executeUpdate() > 0;
        }
    }


    // HISTORIAL DE JUEGOS Y PARTIDAS DE UN SOCIO
    public static String historial(String documento) throws SQLException {

        StringBuilder texto = new StringBuilder();

        texto.append("JUEGOS EN PROPIEDAD:\n");

        String sqlJuegos = "SELECT id, nombre, estado " + "FROM juegos WHERE documento_propietario=? " + "ORDER BY nombre";

        try (Connection conexion = DatabaseConnection.conectar()) {

            try (PreparedStatement sentencia = conexion.prepareStatement(sqlJuegos)) {

                sentencia.setString(1, documento);

                try (ResultSet resultado = sentencia.executeQuery()) {

                    while (resultado.next()) {

                        texto.append("ID: ").append(resultado.getInt("id"));

                        texto.append(" | Juego: ").append(resultado.getString("nombre"));

                        texto.append(" | Estado: ").append(resultado.getString("estado"));

                        texto.append("\n");
                    }
                }
            }

            texto.append("\nPARTIDAS EN LAS QUE PARTICIPA:\n");

            String sqlPartidas = "SELECT p.id, j.nombre, p.fecha, p.hora, i.ganador " + "FROM inscripciones i " + "JOIN partidas p ON i.id_partida=p.id " + "JOIN juegos j ON p.id_juego=j.id " + "WHERE i.documento_socio=? " + "ORDER BY p.fecha DESC, p.hora DESC";

            try (PreparedStatement sentencia = conexion.prepareStatement(sqlPartidas)) {

                sentencia.setString(1, documento);

                try (ResultSet resultado = sentencia.executeQuery()) {

                    while (resultado.next()) {

                        texto.append("Partida: ").append(resultado.getInt("id"));

                        texto.append(" | Juego: ").append(resultado.getString("nombre"));

                        texto.append(" | Fecha: ").append(resultado.getDate("fecha"));

                        texto.append(" | Hora: ").append(resultado.getTime("hora"));

                        if (resultado.getBoolean("ganador")) {
                            texto.append(" | Ganador");
                        }

                        texto.append("\n");
                    }
                }
            }
        }

        return texto.toString();
    }


    // BUSCAR POR NOMBRE O APELLIDOS
    public static List<Socio> buscarPorNombre(String nombre) throws SQLException {

        List<Socio> socios = new ArrayList<>();

        String sql = "SELECT * FROM socios " + "WHERE nombre LIKE ? OR apellidos LIKE ? " + "ORDER BY apellidos, nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            String busqueda = "%" + nombre.trim() + "%";

            sentencia.setString(1, busqueda);
            sentencia.setString(2, busqueda);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    socios.add(convertir(resultado));
                }
            }
        }

        return socios;
    }


    // NUEVO: BUSQUEDA AVANZADA DE SOCIOS
    public static List<Socio> buscarAvanzado(int opcion, String busqueda) throws SQLException {

        List<Socio> socios = new ArrayList<>();

        String sql;

        // Seleccionamos la consulta segun el ComboBox
        if (opcion == 0) {

            // Documento
            sql = "SELECT * FROM socios " + "WHERE documento LIKE ? " + "ORDER BY apellidos, nombre";

        } else if (opcion == 1) {

            // Nombre
            sql = "SELECT * FROM socios " + "WHERE nombre LIKE ? " + "ORDER BY apellidos, nombre";

        } else if (opcion == 2) {

            // Apellidos
            sql = "SELECT * FROM socios " + "WHERE apellidos LIKE ? " + "ORDER BY apellidos, nombre";

        } else if (opcion == 3) {

            // Email
            sql = "SELECT * FROM socios " + "WHERE email LIKE ? " + "ORDER BY apellidos, nombre";

        } else if (opcion == 4) {

            // Edad exacta
            sql = "SELECT * FROM socios " + "WHERE edad=? " + "ORDER BY apellidos, nombre";

        } else {

            throw new IllegalArgumentException("Tipo de busqueda incorrecto");
        }


        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            if (opcion == 4) {

                int edad = Integer.parseInt(busqueda.trim());

                if (edad < 0) {
                    throw new IllegalArgumentException("La edad no puede ser negativa");
                }

                sentencia.setInt(1, edad);

            } else {

                // Permite coincidencias parciales
                sentencia.setString(1, "%" + busqueda.trim() + "%");
            }

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    socios.add(convertir(resultado));
                }
            }
        }

        return socios;
    }


    // CONSULTAR SOCIOS INSCRITOS EN UNA PARTIDA
    public static List<Socio> listarPorPartida(int idPartida) throws SQLException {

        List<Socio> socios = new ArrayList<>();

        String sql = "SELECT s.* FROM socios s " + "JOIN inscripciones i " + "ON i.documento_socio=s.documento " + "WHERE i.id_partida=? " + "ORDER BY s.apellidos, s.nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPartida);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {
                    socios.add(convertir(resultado));
                }
            }
        }

        return socios;
    }
}
