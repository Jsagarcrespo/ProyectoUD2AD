package org.example;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.example.dao.GestionJuegos;
import org.example.dao.GestionPartidas;
import org.example.dao.GestionSocias;
import org.example.models.Juego;
import org.example.models.Partida;
import org.example.models.Socia;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

public class GestionXML {

    // Crea la carpeta donde se guardaran los archivos XML
    private static void crearCarpeta() throws IOException {
        File carpeta = new File("ficheros");

        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se ha podido crear la carpeta ficheros");
        }
    }

    // Exporta todos los juegos de MariaDB al archivo Juegos.xml
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

    // Exporta todas las socias de MariaDB al archivo Socias.xml
    public static void exportarSocias() throws SQLException, IOException {
        ArrayList<Socia> socias = new ArrayList<>(GestionSocias.listar());

        XStream xstream = new XStream(new DomDriver());
        xstream.alias("SociasAsociacion", ArrayList.class);
        xstream.alias("Socia", Socia.class);

        crearCarpeta();

        try (FileOutputStream salida = new FileOutputStream("ficheros/Socias.xml")) {
            xstream.toXML(socias, salida);
        }
    }

    // Exporta todas las partidas de MariaDB al archivo Partidas.xml
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

    // Exporta los tres tipos de datos con una sola opcion del menu
    public static void exportarTodo() throws SQLException, IOException {
        exportarJuegos();
        exportarSocias();
        exportarPartidas();
    }
}
