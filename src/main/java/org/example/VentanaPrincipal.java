package org.example;

import org.example.dao.GestionAsociacion;
import org.example.models.Asociacion;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.SQLException;

public class VentanaPrincipal extends JFrame {

    private JButton juegosButton;
    private JButton sociosButton;
    private JButton partidasButton;
    private JButton inscripcionesButton;
    private JButton asociacionesButton;
    private JButton metadatosButton;
    private JButton exportarXMLButton;
    private JButton exportarJSONButton;
    private JButton SALIRButton;
    private JPanel VentanaPrincipal;


    public VentanaPrincipal() {

        setTitle("Asociacion de Juegos de Mesa");
        setContentPane(VentanaPrincipal);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        // Abre la ventana de juegos
        juegosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                GestionJuegos ventana = new GestionJuegos();
                ventana.setVisible(true);
            }
        });


        // Abre la ventana de socios
        sociosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                VentanaSocios ventana = new VentanaSocios();
                ventana.setVisible(true);
            }
        });


        // Abre la ventana de partidas
        partidasButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VentanaPartidas ventana = new VentanaPartidas();
                ventana.setVisible(true);
            }
        });


        // Abre la ventana de inscripciones
        inscripcionesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                VentanaInscripciones ventana = new VentanaInscripciones();
                ventana.setVisible(true);

            }
        });


        // Muestra los datos de la asociacion
        asociacionesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {


                VentanaAsociacion ventana = new VentanaAsociacion();
                ventana.setVisible(true);

            }
        });


        // Muestra y exporta los metadatos de MariaDB
        metadatosButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {

                    String datos = GestionMetadatos.obtenerMetadatos();

                    JTextArea texto = new JTextArea(datos, 20, 55);
                    texto.setEditable(false);

                    JScrollPane scroll = new JScrollPane(texto);

                    JOptionPane.showMessageDialog(VentanaPrincipal.this, scroll, "Metadatos de MariaDB", JOptionPane.INFORMATION_MESSAGE);

                    GestionMetadatos.exportar();

                    JOptionPane.showMessageDialog(VentanaPrincipal.this, "Metadatos guardados en ficheros/Metadatos.txt");

                } catch (SQLException | IOException ex) {

                    JOptionPane.showMessageDialog(VentanaPrincipal.this, "Error al obtener metadatos: " + ex.getMessage());
                }
            }
        });


        // Exporta los datos en XML
        exportarXMLButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {

                    GestionXML.exportarTodo();

                    JOptionPane.showMessageDialog(VentanaPrincipal.this, "Archivos XML exportados correctamente");

                } catch (SQLException | IOException ex) {

                    JOptionPane.showMessageDialog(VentanaPrincipal.this, "Error al exportar XML: " + ex.getMessage());
                }
            }
        });


        // Exporta los datos en JSON
        exportarJSONButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                try {

                    GestionJSON.exportarTodo();

                    JOptionPane.showMessageDialog(VentanaPrincipal.this, "Archivos JSON exportados correctamente");

                } catch (SQLException | IOException ex) {

                    JOptionPane.showMessageDialog(VentanaPrincipal.this, "Error al exportar JSON: " + ex.getMessage());
                }
            }
        });


        // Cierra la aplicacion
        SALIRButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                System.exit(0);
            }
        });

    }
}

