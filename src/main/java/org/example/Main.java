package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Main {

    public static void main(String[] args) {

        String url = "jdbc:mariadb://localhost:3306/asociacion_juegos";
        String usuario = "juegos_user";
        String password = "juegos123";

        try {

            Connection conexion = DriverManager.getConnection(
                    url, usuario, password
            );

            System.out.println("Conexion correcta con MariaDB");

            conexion.close();

        } catch (SQLException e) {

            System.out.println("Error de conexion: " + e.getMessage());

        }
    }
}