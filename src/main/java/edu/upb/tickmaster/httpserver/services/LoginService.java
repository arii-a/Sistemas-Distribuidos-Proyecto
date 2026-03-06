package edu.upb.tickmaster.httpserver.services;

import com.google.gson.JsonObject;
import edu.upb.tickmaster.httpserver.repositories.LoginRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginService {
    private static final Logger logger = LoggerFactory.getLogger(LoginService.class);
    private final LoginRepository repository = new LoginRepository();

    public JsonObject login(String username, String password) throws SQLException {
        ResultSet rs = repository.findByUsername(username);
        JsonObject result = new JsonObject();

        if (rs.next()) {
            String hashGuardado = rs.getString("password");
            boolean match = BCrypt.checkpw(password, hashGuardado);
            logger.info("BCrypt match: " + match);

            if (match) {
                int id = rs.getInt("id");
                String nombre = rs.getString("nombre");
                int rolId = rs.getInt("rol_id");
                String rol = rolId == 1 ? "admin" : "cliente"; // regla de negocio

                result.addProperty("status", "OK");
                result.addProperty("id", id);
                result.addProperty("nombre", nombre);
                result.addProperty("rol", rol);
            } else {
                result.addProperty("status", "UNAUTHORIZED");
            }
        } else {
            result.addProperty("status", "UNAUTHORIZED");
        }

        return result;
    }
}