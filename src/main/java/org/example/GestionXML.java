package org.example;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.example.dao.GestionJuegos;
import org.example.dao.GestionPartidas;
import org.example.dao.GestionSocios;
import org.example.dao.GestionInscripciones;

import org.example.models.Juego;
import org.example.models.Partida;
import org.example.models.Socio;
import org.example.models.Inscripcion;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class GestionXML {

    // Crea la carpeta donde se guardan los archivos
    private static void crearCarpeta() throws IOException {

        File carpeta = new File("ficheros");

        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se puede crear la carpeta ficheros");
        }
    }


    // Exporta todos los juegos
    public static void exportarJuegos() throws SQLException, IOException {

        ArrayList<Juego> juegos = new ArrayList<>(GestionJuegos.listar());

        XStream xstream = new XStream(new DomDriver());

        xstream.alias("JuegosAsociacion", ArrayList.class);
        xstream.alias("Juego", Juego.class);

        crearCarpeta();

        try (FileOutputStream salida = new FileOutputStream("ficheros/Juegos.xml")) {

            xstream.toXML(juegos, salida);
        }
    }


    // Exporta todos los socios
    public static void exportarSocios() throws SQLException, IOException {

        ArrayList<Socio> socios = new ArrayList<>(GestionSocios.listar());

        XStream xstream = new XStream(new DomDriver());

        xstream.alias("SociosAsociacion", ArrayList.class);
        xstream.alias("Socio", Socio.class);

        crearCarpeta();

        try (FileOutputStream salida = new FileOutputStream("ficheros/Socios.xml")) {

            xstream.toXML(socios, salida);
        }
    }


    // Evita problemas con caracteres especiales en XML
    private static String textoXML(String texto) {

        if (texto == null) {
            return "";
        }

        return texto.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }


    // Exporta partidas, participantes, ganadores y plazas
    public static void exportarPartidas() throws SQLException, IOException {

        List<Partida> partidas = GestionPartidas.listar();

        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<PartidasAsociacion>\n");


        for (Partida partida : partidas) {

            Juego juego = GestionJuegos.buscar(partida.getIdJuego());

            List<Inscripcion> participantes = GestionInscripciones.listar(partida.getId());

            int libres = GestionPartidas.plazasLibres(partida.getId());


            xml.append("  <Partida>\n");

            xml.append("    <id>").append(partida.getId()).append("</id>\n");

            xml.append("    <idJuego>").append(partida.getIdJuego()).append("</idJuego>\n");

            if (juego != null) {

                xml.append("    <nombreJuego>").append(textoXML(juego.getNombre())).append("</nombreJuego>\n");
            }

            xml.append("    <fecha>").append(partida.getFecha()).append("</fecha>\n");

            xml.append("    <hora>").append(partida.getHora()).append("</hora>\n");

            xml.append("    <jugadoresDeseados>").append(partida.getJugadoresDeseados()).append("</jugadoresDeseados>\n");

            xml.append("    <plazasLibres>").append(libres).append("</plazasLibres>\n");

            xml.append("    <estado>").append(textoXML(partida.getEstado())).append("</estado>\n");


            // Participantes de la partida
            xml.append("    <participantes>\n");

            for (Inscripcion inscripcion : participantes) {

                xml.append("      <Participante>\n");

                xml.append("        <idInscripcion>").append(inscripcion.getId()).append("</idInscripcion>\n");


                if (inscripcion.getDocumentoSocio() != null) {

                    // Es un socio de la asociacion
                    Socio socio = GestionSocios.buscar(inscripcion.getDocumentoSocio());

                    String nombre = inscripcion.getDocumentoSocio();

                    if (socio != null) {
                        nombre = socio.getNombre() + " " + socio.getApellidos();
                    }

                    xml.append("        <tipo>socio</tipo>\n");

                    xml.append("        <nombre>").append(textoXML(nombre)).append("</nombre>\n");

                    xml.append("        <documento>").append(textoXML(inscripcion.getDocumentoSocio())).append("</documento>\n");

                } else {

                    // Es una persona invitada
                    xml.append("        <tipo>invitado</tipo>\n");

                    xml.append("        <nombre>").append(textoXML(inscripcion.getNombreInvitado())).append("</nombre>\n");
                }


                xml.append("        <ganador>").append(inscripcion.isGanador()).append("</ganador>\n");

                xml.append("      </Participante>\n");
            }

            xml.append("    </participantes>\n");
            xml.append("  </Partida>\n");
        }


        xml.append("</PartidasAsociacion>\n");

        crearCarpeta();

        Files.writeString(Path.of("ficheros", "Partidas.xml"), xml.toString(), StandardCharsets.UTF_8);
    }


    // Exporta los tres archivos XML
    public static void exportarTodo() throws SQLException, IOException {

        exportarJuegos();
        exportarSocios();
        exportarPartidas();
    }
}
