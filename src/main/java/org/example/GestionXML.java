package org.example;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

import org.example.dao.GestionJuegos;
import org.example.dao.GestionPartidas;
import org.example.dao.GestionSocios;

import org.example.models.Juego;
import org.example.models.Partida;
import org.example.models.Socio;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class GestionXML {

    // Crea la carpeta de exportacion
    private static void crearCarpeta() throws IOException {

        File carpeta = new File("ficheros");

        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se puede crear la carpeta ficheros");
        }
    }

    // Exporta juegos
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

    // Exporta socios
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

    // Exporta partidas
    public static void exportarPartidas() throws SQLException, IOException {

        ArrayList<Partida> partidas = new ArrayList<>(GestionPartidas.listar());

        XStream xstream = new XStream(new DomDriver());

        xstream.alias("PartidasAsociacion", ArrayList.class);
        xstream.alias("Partida", Partida.class);

        crearCarpeta();

        try (FileOutputStream salida = new FileOutputStream("ficheros/Partidas.xml")) {

            xstream.toXML(partidas, salida);
        }
    }

    // Exporta todos los datos
    public static void exportarTodo() throws SQLException, IOException {

        exportarJuegos();
        exportarSocios();
        exportarPartidas();
    }
}
