package org.example.dao;

import org.example.models.Juego;
import org.example.utils.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GestionJuegos {


    // Convierte los datos de la base de datos
    // en un objeto de la clase Juego
    private static Juego convertir(ResultSet resultado) throws SQLException {

        Date fecha = resultado.getDate("fecha_alta");

        return new Juego(resultado.getInt("id"), resultado.getString("nombre"), resultado.getString("autores"), resultado.getString("descripcion"), resultado.getInt("min_jugadores"), resultado.getInt("max_jugadores"), resultado.getInt("duracion_min"), resultado.getString("tematica"), resultado.getString("tipo"), resultado.getString("ubicacion"), resultado.getString("estado"), resultado.getString("documento_propietaria"), fecha == null ? null : fecha.toLocalDate());
    }


    // Obtiene todos los juegos de la base de datos
    public static List<Juego> listar() throws SQLException {

        List<Juego> juegos = new ArrayList<>();

        String sql = "SELECT * FROM juegos ORDER BY nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql); ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {

                Juego juego = convertir(resultado);

                juegos.add(juego);
            }
        }

        return juegos;
    }


    // Busca un juego utilizando su ID
    public static Juego buscar(int id) throws SQLException {

        String sql = "SELECT * FROM juegos WHERE id=?";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            try (ResultSet resultado = sentencia.executeQuery()) {

                if (resultado.next()) {

                    return convertir(resultado);
                }
            }
        }

        return null;
    }


    // Añade un nuevo juego a la base de datos
    public static void anadir(Juego juego) throws SQLException {

        juego.validar();

        String sql = "INSERT INTO juegos " + "(nombre, autores, descripcion, min_jugadores, " + "max_jugadores, duracion_min, tematica, tipo, " + "ubicacion, estado, documento_propietaria, fecha_alta) " + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            ponerCampos(sentencia, juego);

            sentencia.executeUpdate();
        }
    }


    // Modifica los datos de un juego existente
    public static boolean modificar(Juego juego) throws SQLException {

        juego.validar();

        String sql = "UPDATE juegos SET nombre=?, autores=?, " + "descripcion=?, min_jugadores=?, max_jugadores=?, " + "duracion_min=?, tematica=?, tipo=?, ubicacion=?, " + "estado=?, documento_propietaria=?, fecha_alta=? " + "WHERE id=?";

        try (Connection conexion = DatabaseConnection.conectar()) {

            // Comprueba que el nuevo rango de jugadores
            // sea valido para las partidas existentes
            String consulta = "SELECT COUNT(*) FROM partidas " + "WHERE id_juego=? AND estado<>'cancelada' " + "AND (jugadores_deseados<? OR jugadores_deseados>?)";

            try (PreparedStatement comprobacion = conexion.prepareStatement(consulta)) {

                comprobacion.setInt(1, juego.getId());
                comprobacion.setInt(2, juego.getMinJugadores());
                comprobacion.setInt(3, juego.getMaxJugadores());

                try (ResultSet resultado = comprobacion.executeQuery()) {

                    if (resultado.next()) {

                        if (resultado.getInt(1) > 0) {

                            throw new IllegalArgumentException("El nuevo rango de jugadores " + "no sirve para algunas partidas existentes");
                        }
                    }
                }
            }

            try (PreparedStatement sentencia = conexion.prepareStatement(sql)) {

                ponerCampos(sentencia, juego);

                sentencia.setInt(13, juego.getId());

                return sentencia.executeUpdate() > 0;
            }
        }
    }


    // Introduce los valores del juego en el PreparedStatement
    // Se utiliza para añadir y modificar juegos
    private static void ponerCampos(PreparedStatement sentencia, Juego juego) throws SQLException {

        sentencia.setString(1, juego.getNombre());
        sentencia.setString(2, juego.getAutores());
        sentencia.setString(3, juego.getDescripcion());
        sentencia.setInt(4, juego.getMinJugadores());
        sentencia.setInt(5, juego.getMaxJugadores());
        sentencia.setInt(6, juego.getDuracionMin());
        sentencia.setString(7, juego.getTematica());
        sentencia.setString(8, juego.getTipo());
        sentencia.setString(9, juego.getUbicacion());
        sentencia.setString(10, juego.getEstado());

        // Si no tiene propietaria, pertenece a la asociacion
        if (juego.getDocumentoPropietaria() == null || juego.getDocumentoPropietaria().isBlank()) {

            sentencia.setNull(11, Types.VARCHAR);

        } else {

            sentencia.setString(11, juego.getDocumentoPropietaria());
        }

        sentencia.setDate(12, Date.valueOf(juego.getFechaAlta()));
    }


    // Elimina un juego mediante su ID
    public static boolean eliminar(int id) throws SQLException {

        String sql = "DELETE FROM juegos WHERE id=?";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setInt(1, id);

            return sentencia.executeUpdate() > 0;
        }
    }


    // Muestra los juegos indicando su propietaria
    public static String listadoConPropietarias() throws SQLException {

        String sql = "SELECT j.id, j.nombre, j.estado, " + "CASE WHEN s.documento IS NULL " + "THEN 'Asociacion' " + "ELSE CONCAT(s.nombre, ' ', s.apellidos) " + "END AS propietario " + "FROM juegos j " + "LEFT JOIN socias s " + "ON j.documento_propietaria=s.documento " + "ORDER BY j.nombre";

        StringBuilder texto = new StringBuilder();

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql); ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {

                texto.append("ID: ").append(resultado.getInt("id"));
                texto.append(" | Juego: ").append(resultado.getString("nombre"));
                texto.append(" | Estado: ").append(resultado.getString("estado"));
                texto.append(" | Propietaria: ").append(resultado.getString("propietario"));
                texto.append("\n");
            }
        }

        return texto.toString();
    }


    // Busca juegos por su nombre
    public static List<Juego> buscarPorNombre(String nombre) throws SQLException {

        List<Juego> juegos = new ArrayList<>();

        String sql = "SELECT * FROM juegos WHERE nombre LIKE ? " + "ORDER BY nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, "%" + nombre.trim() + "%");

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    juegos.add(convertir(resultado));
                }
            }
        }

        return juegos;
    }


    // Busca juegos segun su estado
    public static List<Juego> listarPorEstado(String estado) throws SQLException {

        List<Juego> juegos = new ArrayList<>();

        String sql = "SELECT * FROM juegos WHERE estado=? ORDER BY nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, estado);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    juegos.add(convertir(resultado));
                }
            }
        }

        return juegos;
    }


    // Muestra los juegos que pertenecen a una socia
    public static List<Juego> listarPorPropietaria(String documento) throws SQLException {

        List<Juego> juegos = new ArrayList<>();

        String sql = "SELECT * FROM juegos " + "WHERE documento_propietaria=? ORDER BY nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql)) {

            sentencia.setString(1, documento);

            try (ResultSet resultado = sentencia.executeQuery()) {

                while (resultado.next()) {

                    juegos.add(convertir(resultado));
                }
            }
        }

        return juegos;
    }


    // Muestra los juegos que pertenecen a la asociacion
    public static List<Juego> listarDeLaAsociacion() throws SQLException {

        List<Juego> juegos = new ArrayList<>();

        String sql = "SELECT * FROM juegos " + "WHERE documento_propietaria IS NULL ORDER BY nombre";

        try (Connection conexion = DatabaseConnection.conectar(); PreparedStatement sentencia = conexion.prepareStatement(sql); ResultSet resultado = sentencia.executeQuery()) {

            while (resultado.next()) {

                juegos.add(convertir(resultado));
            }
        }

        return juegos;
    }
}
