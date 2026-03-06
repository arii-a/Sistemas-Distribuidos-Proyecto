package edu.upb.tickmaster.httpserver.repositories;

import edu.upb.tickmaster.db.ConnectionDB;
import edu.upb.tickmaster.httpserver.handlers.EventPostHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class EventRepository {
    private static final Logger logger = LoggerFactory.getLogger(EventPostHandler.class);

    private Connection getConnection() {
        return ConnectionDB.instance().getConnection();
    }

    public int[] insertClient(String nombre, java.sql.Date fecha, int capacidad) {
        try {
            logger.info("Insertando evento");

            String query = "INSERT INTO public.eventos (nombre, fecha, capacidad) VALUES (?, ?, ?)";
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, nombre);
            statement.setDate(2, fecha);
            statement.setInt(3, capacidad);

            int rows = statement.executeUpdate();
             /*if ("backend-1".equals(instanceId)) {
                 Thread.sleep(3000);
             }*/
            //Thread.sleep(2000); //probando funcionamiento del read timeout
            statement.close();

            if (rows > 0) {
                logger.info("Evento creado exitosamente.");
                return new int[]{201};
            }

        } catch (SQLException e) {
            logger.warn("Intento fallido: " + e.getMessage());
        } /*catch (InterruptedException e) {
             throw new RuntimeException(e); //solo usado para sleep
         }*/

        return new int[]{500};
    }
}
