package edu.upb.tickmaster.httpserver.repositories;

import edu.upb.tickmaster.db.ConnectionDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class ClientRepository {
    private static final Logger logger = LoggerFactory.getLogger(ClientRepository.class);

    private Connection getConnection() {
        return ConnectionDB.instance().getConnection();
    }

    public int[] insertClient(String user, String nombre, String pass, int rol) {
        try {
            logger.info("Insertando usuario");

            String query = "INSERT INTO public.users (username, nombre, password, rol_id) VALUES (?, ?, ?, ?)";
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setString(1, user);
            statement.setString(2, nombre);
            statement.setString(3, pass);
            statement.setInt(4, rol);

            int rows = statement.executeUpdate();
             /*if ("backend-1".equals(instanceId)) {
                 Thread.sleep(3000);
             }*/
            //Thread.sleep(2000); //probando funcionamiento del read timeout
            statement.close();

            if (rows > 0) {
                logger.info("Usuario creado exitosamente.");
                return new int[]{201};
            }

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                logger.info("Usuario duplicado: " + e.getMessage());
                return new  int[]{409};
            }
            logger.warn("Intento fallido: " + e.getMessage());

        } /*catch (InterruptedException e) {
             throw new RuntimeException(e); //solo usado para sleep
            }*/

        return new int[]{500};
    }
}
