package org.example;

import org.example.dao.GestionInscripciones;
import org.example.dao.GestionJuegos;
import org.example.dao.GestionPartidas;
import org.example.dao.GestionSocios;

import org.example.models.Inscripcion;
import org.example.models.Juego;
import org.example.models.Partida;
import org.example.models.Socio;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class GestionJSON {

    // Guarda un archivo en la carpeta ficheros
    private static void guardar(String archivo, String texto) throws IOException {

        Files.createDirectories(Path.of("ficheros"));

        Files.writeString(Path.of("ficheros", archivo), texto, StandardCharsets.UTF_8);
    }

    // Convierte un valor de Java a JSON
    private static String valor(Object dato) {

        if (dato == null) {
            return "null";
        }

        if (dato instanceof Number || dato instanceof Boolean) {
            return dato.toString();
        }

        String texto = dato.toString();
        StringBuilder resultado = new StringBuilder("\"");

        for (int i = 0; i < texto.length(); i++) {

            char letra = texto.charAt(i);

            switch (letra) {
                case '"':
                    resultado.append("\\\"");
                    break;
                case '\\':
                    resultado.append("\\\\");
                    break;
                case '\n':
                    resultado.append("\\n");
                    break;
                case '\r':
                    resultado.append("\\r");
                    break;
                case '\t':
                    resultado.append("\\t");
                    break;
                default:
                    if (letra < 32) {
                        resultado.append(String.format("\\u%04x", (int) letra));
                    } else {
                        resultado.append(letra);
                    }
            }
        }

        resultado.append("\"");
        return resultado.toString();
    }

    // Añade un campo al texto JSON
    private static void campo(StringBuilder json, String nombre, Object dato, boolean ultimo, int espacios) {

        json.append(" ".repeat(espacios)).append(valor(nombre)).append(": ").append(valor(dato));

        if (ultimo) {
            json.append("\n");
        } else {
            json.append(",\n");
        }
    }

    // Exporta los juegos
    public static void exportarJuegos() throws SQLException, IOException {

        List<Juego> juegos = GestionJuegos.listar();

        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < juegos.size(); i++) {

            Juego juego = juegos.get(i);

            json.append("  {\n");

            campo(json, "id", juego.getId(), false, 4);
            campo(json, "nombre", juego.getNombre(), false, 4);
            campo(json, "autores", juego.getAutores(), false, 4);
            campo(json, "descripcion", juego.getDescripcion(), false, 4);
            campo(json, "min_jugadores", juego.getMinJugadores(), false, 4);
            campo(json, "max_jugadores", juego.getMaxJugadores(), false, 4);
            campo(json, "duracion_min", juego.getDuracionMin(), false, 4);
            campo(json, "tematica", juego.getTematica(), false, 4);
            campo(json, "tipo", juego.getTipo(), false, 4);
            campo(json, "ubicacion", juego.getUbicacion(), false, 4);
            campo(json, "estado", juego.getEstado(), false, 4);
            campo(json, "documento_propietario", juego.getDocumentoPropietario(), false, 4);
            campo(json, "fecha_alta", juego.getFechaAlta(), true, 4);

            json.append("  }");

            if (i < juegos.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("]\n");

        guardar("Juegos.json", json.toString());
    }

    // Exporta los socios
    public static void exportarSocios() throws SQLException, IOException {

        List<Socio> socios = GestionSocios.listar();

        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < socios.size(); i++) {

            Socio socio = socios.get(i);

            json.append("  {\n");

            campo(json, "documento", socio.getDocumento(), false, 4);
            campo(json, "nombre", socio.getNombre(), false, 4);
            campo(json, "apellidos", socio.getApellidos(), false, 4);
            campo(json, "telefono", socio.getTelefono(), false, 4);
            campo(json, "edad", socio.getEdad(), false, 4);
            campo(json, "email", socio.getEmail(), true, 4);

            json.append("  }");

            if (i < socios.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("]\n");

        guardar("Socios.json", json.toString());
    }

    // Exporta partidas con participantes y plazas libres
    public static void exportarPartidas() throws SQLException, IOException {

        List<Partida> partidas = GestionPartidas.listar();

        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < partidas.size(); i++) {

            Partida partida = partidas.get(i);

            Juego juego = GestionJuegos.buscar(partida.getIdJuego());

            List<Inscripcion> participantes = GestionInscripciones.listar(partida.getId());

            json.append("  {\n");

            campo(json, "id", partida.getId(), false, 4);
            campo(json, "id_juego", partida.getIdJuego(), false, 4);

            campo(json, "nombre_juego", juego == null ? null : juego.getNombre(), false, 4);

            campo(json, "fecha", partida.getFecha(), false, 4);
            campo(json, "hora", partida.getHora(), false, 4);

            campo(json, "jugadores_deseados", partida.getJugadoresDeseados(), false, 4);

            campo(json, "plazas_libres", GestionPartidas.plazasLibres(partida.getId()), false, 4);

            campo(json, "estado", partida.getEstado(), false, 4);

            json.append("    \"participantes\": [\n");

            for (int j = 0; j < participantes.size(); j++) {

                Inscripcion inscripcion = participantes.get(j);

                String nombre;
                String tipo;

                if (inscripcion.getDocumentoSocio() != null) {

                    tipo = "socio";

                    Socio socio = GestionSocios.buscar(inscripcion.getDocumentoSocio());

                    if (socio == null) {
                        nombre = null;
                    } else {
                        nombre = socio.getNombre() + " " + socio.getApellidos();
                    }

                } else {

                    tipo = "invitado";
                    nombre = inscripcion.getNombreInvitado();
                }

                json.append("      {\n");

                campo(json, "id_inscripcion", inscripcion.getId(), false, 8);

                campo(json, "tipo", tipo, false, 8);
                campo(json, "nombre", nombre, false, 8);

                campo(json, "documento_socio", inscripcion.getDocumentoSocio(), false, 8);

                campo(json, "ganador", inscripcion.isGanador(), true, 8);

                json.append("      }");

                if (j < participantes.size() - 1) {
                    json.append(",");
                }

                json.append("\n");
            }

            json.append("    ]\n");
            json.append("  }");

            if (i < partidas.size() - 1) {
                json.append(",");
            }

            json.append("\n");
        }

        json.append("]\n");

        guardar("Partidas.json", json.toString());
    }

    // Exporta todos los archivos JSON
    public static void exportarTodo() throws SQLException, IOException {

        exportarJuegos();
        exportarSocios();
        exportarPartidas();
    }

    // Prueba individual
    public static void main(String[] args) {

        try {

            exportarTodo();
            System.out.println("JSON exportados correctamente");

        } catch (SQLException | IOException e) {

            System.out.println("Error al exportar JSON: " + e.getMessage());
        }
    }
}
