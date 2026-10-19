package org.example;

import org.example.models.Juego;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class GestionJuegos extends JFrame {
    private JTextField NombreJuego;
    private JTextField Autores;
    private JTextField Descripcion;
    private JTextField MaxJugadores;
    private JTextField MinJugadors;
    private JTextField Duracion;
    private JTextField Tematica;
    private JTextField Tipo;
    private JTextField Ubicacion;
    private JTextField Estado;
    private JTextField Propietario;
    private JTextField FechaAlta;
    private JButton añadirButton;
    private JButton eliminarButton;
    private JButton modificarButton;
    private JTextField BuscarTextField;
    private JButton BUSCARButton;
    private JTextArea Juegos;
    private JButton mostrarTodoButton;
    private JButton limpiarButton;
    private JButton cerrarButton;
    private JTextField IdJuego;
    private JPanel GestionJuegos;

    public GestionJuegos() {

        setTitle("Gestion juegos");
        setContentPane(GestionJuegos);
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        añadirButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        modificarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        eliminarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        BUSCARButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        mostrarTodoButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        limpiarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        cerrarButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

            }
        });
        Juegos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
        });



    }

    public void mostrarJugos(){
        ArrayList<Juego> juegos = GestionJuegos.;




    }


}
