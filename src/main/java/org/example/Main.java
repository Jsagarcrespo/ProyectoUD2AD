package org.example;

import org.example.dao.GestionAsociacion;
import org.example.dao.GestionInscripciones;
import org.example.dao.GestionPartidas;
import org.example.dao.GestionSocios;
import org.example.models.*;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.List;

public class Main {

    static BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

    public static void main(String[] args) throws IOException {


        SwingUtilities.invokeLater(() -> {

            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);

        });


    }


}

