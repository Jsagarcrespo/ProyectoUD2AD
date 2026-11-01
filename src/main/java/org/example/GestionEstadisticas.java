package org.example;

import org.example.utils.DatabaseConnection;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalDate;

public class GestionEstadisticas {

    // Calcula las estadisticas y guarda un resumen en JSON y XML.
    public static String exportar(LocalDate desde, LocalDate hasta) throws SQLException, IOException {

        if (desde == null || hasta == null || desde.isAfter(hasta)) {
            throw new IllegalArgumentException("El rango de fechas no es valido");
        }

        int totalSocios;
        int totalJuegos;
        int partidas;
        int finalizadas;
        int canceladas;
        int juegosUtilizados;
        int participacionesSocios;
        int participacionesInvitados;

        // Todas las consultas se hacen en la misma conexion JDBC.
        try (Connection c = DatabaseConnection.conectar()) {

            // Socios y juegos que existen actualmente (no tenemos fecha de alta de socio).
            totalSocios = contar(c, "SELECT COUNT(*) FROM socios");
            totalJuegos = contar(c, "SELECT COUNT(*) FROM juegos");

            String sqlPartidas = "SELECT COUNT(*) AS total, "
                    + "COALESCE(SUM(estado='finalizada'), 0) AS finalizadas, "
                    + "COALESCE(SUM(estado='cancelada'), 0) AS canceladas, "
                    + "COUNT(DISTINCT id_juego) AS juegos "
                    + "FROM partidas WHERE fecha BETWEEN ? AND ?";

            try (PreparedStatement p = c.prepareStatement(sqlPartidas)) {
                p.setDate(1, Date.valueOf(desde));
                p.setDate(2, Date.valueOf(hasta));

                try (ResultSet r = p.executeQuery()) {
                    r.next();
                    partidas = r.getInt("total");
                    finalizadas = r.getInt("finalizadas");
                    canceladas = r.getInt("canceladas");
                    juegosUtilizados = r.getInt("juegos");
                }
            }

            String sqlInscripciones = "SELECT "
                    + "COALESCE(SUM(CASE WHEN i.documento_socio IS NOT NULL THEN 1 ELSE 0 END), 0) AS socios, "
                    + "COALESCE(SUM(CASE WHEN i.documento_socio IS NULL THEN 1 ELSE 0 END), 0) AS invitados "
                    + "FROM inscripciones i JOIN partidas p ON i.id_partida=p.id "
                    + "WHERE p.fecha BETWEEN ? AND ?";

            try (PreparedStatement p = c.prepareStatement(sqlInscripciones)) {
                p.setDate(1, Date.valueOf(desde));
                p.setDate(2, Date.valueOf(hasta));

                try (ResultSet r = p.executeQuery()) {
                    r.next();
                    participacionesSocios = r.getInt("socios");
                    participacionesInvitados = r.getInt("invitados");
                }
            }
        }

        // Generar JSON: las fechas son ISO y los demas valores son numeros.
        String json = "{\n"
                + "  \"fecha_desde\": \"" + desde + "\",\n"
                + "  \"fecha_hasta\": \"" + hasta + "\",\n"
                + "  \"socios_actuales\": " + totalSocios + ",\n"
                + "  \"juegos_actuales\": " + totalJuegos + ",\n"
                + "  \"partidas_periodo\": " + partidas + ",\n"
                + "  \"partidas_finalizadas\": " + finalizadas + ",\n"
                + "  \"partidas_canceladas\": " + canceladas + ",\n"
                + "  \"juegos_utilizados_periodo\": " + juegosUtilizados + ",\n"
                + "  \"participaciones_socios\": " + participacionesSocios + ",\n"
                + "  \"participaciones_invitados\": " + participacionesInvitados + "\n"
                + "}\n";

        // Generar XML: las etiquetas y las fechas son seguras para este formato.
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<estadisticas>\n"
                + "  <fecha_desde>" + desde + "</fecha_desde>\n"
                + "  <fecha_hasta>" + hasta + "</fecha_hasta>\n"
                + "  <socios_actuales>" + totalSocios + "</socios_actuales>\n"
                + "  <juegos_actuales>" + totalJuegos + "</juegos_actuales>\n"
                + "  <partidas_periodo>" + partidas + "</partidas_periodo>\n"
                + "  <partidas_finalizadas>" + finalizadas + "</partidas_finalizadas>\n"
                + "  <partidas_canceladas>" + canceladas + "</partidas_canceladas>\n"
                + "  <juegos_utilizados_periodo>" + juegosUtilizados + "</juegos_utilizados_periodo>\n"
                + "  <participaciones_socios>" + participacionesSocios + "</participaciones_socios>\n"
                + "  <participaciones_invitados>" + participacionesInvitados + "</participaciones_invitados>\n"
                + "</estadisticas>\n";

        Files.createDirectories(Path.of("ficheros"));
        Files.writeString(Path.of("ficheros", "Estadisticas.json"), json, StandardCharsets.UTF_8);
        Files.writeString(Path.of("ficheros", "Estadisticas.xml"), xml, StandardCharsets.UTF_8);

        return "ESTADISTICAS DE LA ASOCIACION\n"
                + "Desde: " + desde + "    Hasta: " + hasta + "\n\n"
                + "Socios actuales: " + totalSocios + "\n"
                + "Juegos actuales: " + totalJuegos + "\n\n"
                + "Partidas en el periodo: " + partidas + "\n"
                + "  Finalizadas: " + finalizadas + "\n"
                + "  Canceladas: " + canceladas + "\n"
                + "  Juegos distintos utilizados: " + juegosUtilizados + "\n"
                + "Participaciones de socios: " + participacionesSocios + "\n"
                + "Participaciones de invitados: " + participacionesInvitados + "\n\n"
                + "NOTA: los totales de socios y juegos son actuales, sin filtro de fecha.\n"
                + "Archivos: ficheros/Estadisticas.json y ficheros/Estadisticas.xml";
    }

    private static int contar(Connection c, String sql) throws SQLException {
        try (PreparedStatement p = c.prepareStatement(sql); ResultSet r = p.executeQuery()) {
            r.next();
            return r.getInt(1);
        }
    }
}
