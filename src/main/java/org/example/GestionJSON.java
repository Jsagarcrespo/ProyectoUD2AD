package org.example;

import org.example.dao.GestionInscripciones;
import org.example.dao.GestionJuegos;
import org.example.dao.GestionPartidas;
import org.example.dao.GestionSocias;
import org.example.models.Inscripcion;
import org.example.models.Juego;
import org.example.models.Partida;
import org.example.models.Socia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

public class GestionJSON {

    // Guarda el texto JSON en la carpeta ficheros
    private static void guardar(String nombreArchivo, String texto) throws IOException {

        Files.createDirectories(Path.of("ficheros"));

        Files.writeString(Path.of("ficheros", nombreArchivo), texto, StandardCharsets.UTF_8);
    }


    // Convierte un dato de Java en un valor JSON valido
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

        return resultado.append('"').toString();
    }


    // Escribe un campo JSON
    private static void campo(StringBuilder json, String nombre, Object dato, boolean ultimo, int espacios) {

        json.append(" ".repeat(espacios)).append(valor(nombre)).append(": ").append(valor(dato));

        json.append(ultimo ? "\n" : ",\n");
    }


    // Exporta todos los juegos
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
            campo(json, "documento_propietaria", juego.getDocumentoPropietaria(), false, 4);
            campo(json, "fecha_alta", juego.getFechaAlta(), true, 4);

            json.append("  }").append(i < juegos.size() - 1 ? ",\n" : "\n");
        }

        json.append("]\n");

        guardar("Juegos.json", json.toString());
    }


    // Exporta todas las socias
    public static void exportarSocias() throws SQLException, IOException {

        List<Socia> socias = GestionSocias.listar();

        StringBuilder json = new StringBuilder("[\n");

        for (int i = 0; i < socias.size(); i++) {

            Socia socia = socias.get(i);

            json.append("  {\n");

            campo(json, "documento", socia.getDocumento(), false, 4);
            campo(json, "nombre", socia.getNombre(), false, 4);
            campo(json, "apellidos", socia.getApellidos(), false, 4);
            campo(json, "telefono", socia.getTelefono(), false, 4);
            campo(json, "edad", socia.getEdad(), false, 4);
            campo(json, "email", socia.getEmail(), true, 4);

            json.append("  }").append(i < socias.size() - 1 ? ",\n" : "\n");
        }

        json.append("]\n");

        guardar("Socias.json", json.toString());
    }


    // Exporta partidas, participantes y plazas libres
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


            // Recorre las personas inscritas en la partida
            for (int j = 0; j < participantes.size(); j++) {

                Inscripcion inscripcion = participantes.get(j);

                String nombre;
                String tipo;

                if (inscripcion.getDocumentoSocia() != null) {

                    tipo = "socia";

                    Socia socia = GestionSocias.buscar(inscripcion.getDocumentoSocia());

                    nombre = socia == null ? null : socia.getNombre() + " " + socia.getApellidos();

                } else {

                    tipo = "invitada";
                    nombre = inscripcion.getNombreInvitada();
                }

                json.append("      {\n");

                campo(json, "id_inscripcion", inscripcion.getId(), false, 8);

                campo(json, "tipo", tipo, false, 8);
                campo(json, "nombre", nombre, false, 8);

                campo(json, "documento_socia", inscripcion.getDocumentoSocia(), false, 8);

                campo(json, "ganadora", inscripcion.isGanadora(), true, 8);

                json.append("      }").append(j < participantes.size() - 1 ? ",\n" : "\n");
            }

            json.append("    ]\n");

            json.append("  }").append(i < partidas.size() - 1 ? ",\n" : "\n");
        }

        json.append("]\n");

        guardar("Partidas.json", json.toString());
    }


    // Exporta los tres archivos
    public static void exportarTodo() throws SQLException, IOException {

        exportarJuegos();
        exportarSocias();
        exportarPartidas();
    }


    // Permite probar la exportacion directamente
    public static void main(String[] args) {

        try {

            exportarTodo();

            System.out.println("Exportacion JSON correcta en la carpeta ficheros");

        } catch (SQLException | IOException e) {

            System.out.println("Error al exportar JSON: " + e.getMessage());
        }
    }
}
