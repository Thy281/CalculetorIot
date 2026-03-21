package com.calculator.Iot;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.sql.ResultSet;

public class DatabaseInitializer {
    public static void main(String[] args) {
        String url = "jdbc:postgresql://151.243.24.227:5432/postgres";
        String user = "postgres";
        String password = "postgres";

        try {
            Class.forName("org.postgresql.Driver");
            Connection conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to PostgreSQL server successfully!");

            // Check if database exists
            ResultSet rs = conn.createStatement().executeQuery(
                "SELECT 1 FROM pg_database WHERE datname = 'calculetoriot'"
            );

            if (!rs.next()) {
                System.out.println("Database 'calculetoriot' does not exist. Creating...");
                Statement stmt = conn.createStatement();
                stmt.executeUpdate("CREATE DATABASE calculetoriot");
                System.out.println("Database 'calculetoriot' created successfully!");
            } else {
                System.out.println("Database 'calculetoriot' already exists.");
            }

            conn.close();
            System.out.println("Done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
