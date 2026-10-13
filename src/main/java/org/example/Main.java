package org.example;

import org.example.utils.DatabaseConnection;

import java.sql.*;

public class Main {

    public static void main(String[] args) {


        try {

            Connection conexion = DatabaseConnection.conectar();

            System.out.println("Conexion correcta con MariaDB");

            Statement sentencia = conexion.createStatement();

            ResultSet resultado = sentencia.executeQuery("SELECT * FROM asociacion");

            while (resultado.next()) {

                System.out.println("Nombre: " +
                        resultado.getString("razon_social"));

                System.out.println("CIF: " +
                        resultado.getString("cif"));

                System.out.println("Direccion: " +
                        resultado.getString("direccion"));

                System.out.println("Telefono: " +
                        resultado.getString("telefono"));

                System.out.println("Email: " +
                        resultado.getString("email"));

            }

            resultado.close();
            sentencia.close();
            conexion.close();

        } catch (SQLException e) {

            System.out.println("Error de conexion: " + e.getMessage());

        }
    }
}