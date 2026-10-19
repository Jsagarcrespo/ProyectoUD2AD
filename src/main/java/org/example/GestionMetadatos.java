package org.example;

import org.example.utils.DatabaseConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public class GestionMetadatos {


    // Obtiene informacion de la base de datos
    public static String obtenerMetadatos() throws SQLException {

        StringBuilder texto = new StringBuilder();

        try (Connection conexion = DatabaseConnection.conectar()) {

            DatabaseMetaData datosBD = conexion.getMetaData();

            String baseDatos = conexion.getCatalog();

            texto.append("===== INFORMACION DE LA BASE DE DATOS =====\n\n");

            texto.append("Base de datos: ").append(baseDatos).append("\n");

            texto.append("Gestor: ").append(datosBD.getDatabaseProductName()).append("\n");

            texto.append("Version: ").append(datosBD.getDatabaseProductVersion()).append("\n");

            texto.append("Driver: ").append(datosBD.getDriverName()).append("\n");

            texto.append("Version del driver: ").append(datosBD.getDriverVersion()).append("\n");

            texto.append("URL: ").append(datosBD.getURL()).append("\n");

            texto.append("Usuario: ").append(datosBD.getUserName()).append("\n");


            // Obtiene las tablas de la base de datos
            texto.append("\n===== TABLAS DE LA BASE DE DATOS =====\n");

            try (ResultSet tablas = datosBD.getTables(baseDatos, null, "%", new String[]{"TABLE"})) {

                while (tablas.next()) {

                    String nombreTabla = tablas.getString("TABLE_NAME");

                    texto.append("\n---------------------------------\n");
                    texto.append("TABLA: ").append(nombreTabla).append("\n");


                    // Consulta las claves primarias de cada tabla
                    texto.append("\nCLAVES PRIMARIAS:\n");

                    boolean tieneClave = false;

                    try (ResultSet claves = datosBD.getPrimaryKeys(baseDatos, null, nombreTabla)) {

                        while (claves.next()) {

                            tieneClave = true;

                            texto.append("- ").append(claves.getString("COLUMN_NAME")).append("\n");
                        }
                    }

                    if (!tieneClave) {
                        texto.append("Sin clave primaria\n");
                    }


                    // Consulta las columnas de cada tabla
                    texto.append("\nCOLUMNAS:\n");

                    String tablaSegura = nombreTabla.replace("`", "``");

                    String sql = "SELECT * FROM `" + tablaSegura + "` LIMIT 0";

                    try (Statement sentencia = conexion.createStatement(); ResultSet resultado = sentencia.executeQuery(sql)) {

                        ResultSetMetaData datosColumnas = resultado.getMetaData();

                        int numeroColumnas = datosColumnas.getColumnCount();

                        for (int i = 1; i <= numeroColumnas; i++) {

                            texto.append("- ").append(datosColumnas.getColumnName(i));

                            texto.append(" | Tipo: ").append(datosColumnas.getColumnTypeName(i));

                            texto.append(" | Tamaño: ").append(datosColumnas.getPrecision(i));

                            if (datosColumnas.isNullable(i) == ResultSetMetaData.columnNoNulls) {

                                texto.append(" | NOT NULL");

                            } else {

                                texto.append(" | Puede ser NULL");
                            }

                            texto.append("\n");
                        }
                    }
                }
            }
        }

        return texto.toString();
    }


    // Muestra la informacion por consola
    public static void mostrar() throws SQLException {

        System.out.println(obtenerMetadatos());
    }


    // Exporta la informacion a un archivo TXT
    public static void exportar() throws SQLException, IOException {

        String texto = obtenerMetadatos();

        Files.createDirectories(Path.of("ficheros"));

        Files.writeString(Path.of("ficheros", "Metadatos.txt"), texto, StandardCharsets.UTF_8);
    }


    // Permite probar la clase sin utilizar el menu principal
    public static void main(String[] args) {

        try {

            mostrar();

            exportar();

            System.out.println("\nMetadatos exportados correctamente.");

        } catch (SQLException | IOException e) {

            System.out.println("Error al obtener metadatos: " + e.getMessage());
        }
    }
}
