package com.currencyconverter.util;

import java.sql.Connection;
import java.sql.DriverManager;

public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/currency_converter";
    private static final String USER     = "root";
    private static final String PASSWORD = "your_password_here";

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}