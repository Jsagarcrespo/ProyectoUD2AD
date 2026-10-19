package org.example.dao;

import org.example.models.Socia;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionSocias {


    // Convierte un registro de MariaDB en un objeto Socia
    private static Socia convertir(ResultSet resultado) throws SQLException {

        return new Socia(resultado.getString("documento"), resultado.getString("nombre"), resultado.getString("apellidos"), resultado.getString("telefono"), resultado.getInt("edad"), resultado.getString("email"));
    }


    // Obtiene todas las socias de la base de datos
    public static List<Socia> listar() throws SQLException {

        List<Socia> socias = new ArrayList<>();

        String sql = "SELECT * FROM socias ORDER BY apellidos, nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql); ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {

                Socia socia = convertir(resultado);

                socias.add(socia);
            }
        }

        return socias;
    }


    // Busca una socia utilizando su DNI, NIE o pasaporte
    public static Socia buscar(String documento) throws SQLException {

        String sql = "SELECT * FROM socias WHERE documento=?";

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


    // Añade una nueva socia a la base de datos
    public static void anadir(Socia socia) throws SQLException {

        socia.validar();

        String sql = "INSERT INTO socias " + "(documento, nombre, apellidos, telefono, edad, email) " + "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, socia.getDocumento());
            sentencia.setString(2, socia.getNombre());
            sentencia.setString(3, socia.getApellidos());
            sentencia.setString(4, socia.getTelefono());
            sentencia.setInt(5, socia.getEdad());
            sentencia.setString(6, socia.getEmail());

            sentencia.executeUpdate();
        }
    }


    // Modifica los datos de una socia existente
    public static boolean modificar(Socia socia) throws SQLException {

        socia.validar();

        String sql = "UPDATE socias SET nombre=?, apellidos=?, " + "telefono=?, edad=?, email=? WHERE documento=?";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, socia.getNombre());
            sentencia.setString(2, socia.getApellidos());
            sentencia.setString(3, socia.getTelefono());
            sentencia.setInt(4, socia.getEdad());
            sentencia.setString(5, socia.getEmail());
            sentencia.setString(6, socia.getDocumento());

            return sentencia.executeUpdate() > 0;
        }
    }


    // Elimina una socia utilizando su documento
    public static boolean eliminar(String documento) throws SQLException {

        String sql = "DELETE FROM socias WHERE documento=?";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, documento);

            return sentencia.executeUpdate() > 0;
        }
    }


    // Muestra los juegos que pertenecen a una socia
    // y las partidas en las que ha participado
    public static String historial(String documento) throws SQLException {

        StringBuilder texto = new StringBuilder();

        texto.append("JUEGOS EN PROPIEDAD:\n");

        String sqlJuegos = "SELECT id, nombre, estado " + "FROM juegos WHERE documento_propietaria=? " + "ORDER BY nombre";

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

            String sqlPartidas = "SELECT p.id, j.nombre, p.fecha, " + "p.hora, i.ganadora " + "FROM inscripciones i " + "JOIN partidas p ON i.id_partida=p.id " + "JOIN juegos j ON p.id_juego=j.id " + "WHERE i.documento_socia=? " + "ORDER BY p.fecha DESC, p.hora DESC";

            try (PreparedStatement sentencia = conexion.prepareStatement(sqlPartidas)) {

                sentencia.setString(1, documento);

                try (ResultSet resultado = sentencia.executeQuery()) {

                    while (resultado.next()) {

                        texto.append("Partida: ").append(resultado.getInt("id"));

                        texto.append(" | Juego: ").append(resultado.getString("nombre"));

                        texto.append(" | Fecha: ").append(resultado.getDate("fecha"));

                        texto.append(" | Hora: ").append(resultado.getTime("hora"));

                        if (resultado.getBoolean("ganadora")) {
                            texto.append(" | Ganadora");
                        }

                        texto.append("\n");
                    }
                }
            }
        }

        return texto.toString();
    }


    // Busca socias por nombre o apellidos
    public static List<Socia> buscarPorNombre(String nombre) throws SQLException {

        List<Socia> socias = new ArrayList<>();

        String sql = "SELECT * FROM socias " + "WHERE nombre LIKE ? OR apellidos LIKE ? " + "ORDER BY apellidos, nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            String busqueda = "%" + nombre.trim() + "%";

            sentencia.setString(1, busqueda);
            sentencia.setString(2, busqueda);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    socias.add(convertir(resultado));
                }
            }
        }

        return socias;
    }


    // Obtiene las socias inscritas en una partida
    public static List<Socia> listarPorPartida(int idPartida) throws SQLException {

        List<Socia> socias = new ArrayList<>();

        String sql = "SELECT s.* FROM socias s " + "JOIN inscripciones i " + "ON i.documento_socia=s.documento " + "WHERE i.id_partida=? " + "ORDER BY s.apellidos, s.nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, idPartida);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    socias.add(convertir(resultado));
                }
            }
        }

        return socias;
    }
}
