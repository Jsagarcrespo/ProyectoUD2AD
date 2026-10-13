package org.example.utils;

import java.sql.*;

public class DatabaseConnection {


    private static final String URL = "jdbc:mariadb://localhost:3306/asociacion_juegos";

    private static final String USUARIO = "juegos_user";
    private static final String PASSWORD = "juegos123";

    public static Connection conectar() throws SQLException {

        return DriverManager.getConnection(URL, USUARIO, PASSWORD);
    }
    

}
