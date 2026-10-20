package org.example;

import org.example.dao.GestionAsociacion;
import org.example.models.Asociacion;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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

    // Etiquetas para los datos de la asociacion
    private JLabel RazonSocialLabel;
    private JLabel CIFLabel;
    private JLabel DireccionLabel;
    private JLabel AperturaLabel;


    public VentanaPrincipal() {

        setTitle("Asociacion de Juegos de Mesa");
        setContentPane(VentanaPrincipal);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Cargar los datos al abrir la aplicacion
        cargarAsociacion();


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


        // Abre los datos de la asociacion
        asociacionesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                VentanaAsociacion ventana = new VentanaAsociacion();

                // Al cerrar, actualizamos los datos del menu
                ventana.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {

                        cargarAsociacion();
                    }
                });

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


    // CARGAR DATOS DE LA ASOCIACION
    public void cargarAsociacion() {

        try {

            // Consultamos MariaDB mediante nuestro DAO
            Asociacion asociacion = GestionAsociacion.obtener();

            if (asociacion != null) {

                RazonSocialLabel.setText(asociacion.getRazonSocial());

                CIFLabel.setText(asociacion.getCif());

                DireccionLabel.setText(asociacion.getDireccion());

                // Comprobamos si tiene fecha de apertura
                if (asociacion.getFechaApertura() != null) {

                    AperturaLabel.setText(asociacion.getFechaApertura().toString());

                } else {

                    AperturaLabel.setText("Sin fecha");
                }

            } else {

                RazonSocialLabel.setText("Sin datos");
                CIFLabel.setText("Sin datos");
                DireccionLabel.setText("Sin datos");
                AperturaLabel.setText("Sin datos");

                JOptionPane.showMessageDialog(this, "No se han encontrado los datos de la asociacion");
            }

        } catch (SQLException e) {

            RazonSocialLabel.setText("Sin conexion");
            CIFLabel.setText("");
            DireccionLabel.setText("");
            AperturaLabel.setText("");

            JOptionPane.showMessageDialog(this, "Error al cargar la asociacion: " + e.getMessage());
        }
    }
}
