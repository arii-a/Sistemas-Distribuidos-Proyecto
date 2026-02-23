package edu.upb.tickmaster.db;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import edu.upb.tickmaster.proxy.ReverseProxyServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Es una clase Singleton,
 * @author Usuario
 */
public class ConnectionDB {
    private final String url = "jdbc:postgresql://localhost:5432/client_ticket";
    private String user = "postgres";
    private String contraseña = "1234567890";
    private int port;

    private static final Logger logger = LoggerFactory.getLogger(ConnectionDB.class);

    private static final ConnectionDB db ;
    private Connection connection;

    static{
        db = new ConnectionDB();
    }

    // constructor privado
    private ConnectionDB() {
        //System.out.println("Iniciando Singleton de Base de Datos...");
        try {
            // Establish the connection once
            this.connection = DriverManager.getConnection(url, user, contraseña);
        } catch (SQLException e) {
            logger.error("Error al conectar a postgreSQL: " + e.getMessage());
        }
    }

    public static ConnectionDB instance() { // Fixed typo from 'intance'
        return db;
    }

    public Connection getConnection() {
        try {
            // Check if connection is closed and reopen if necessary
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(url, user, contraseña);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    public static void main(String[] args) {
        ConnectionDB conn = new ConnectionDB();
        conn.getConnection();
    }
}
