package edu.upb.tickmaster.httpserver.repositories;

import edu.upb.tickmaster.db.ConnectionDB;
import edu.upb.tickmaster.httpserver.handlers.LoginHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class LoginRepository {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    private Connection getConnection() {
        return ConnectionDB.instance().getConnection();
    }

    public ResultSet findByUsername(String username) throws SQLException {
        String sql = "SELECT id, nombre, password, rol_id FROM users WHERE username = ?";
        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setString(1, username);
        ResultSet rs = stmt.executeQuery();

        return stmt.executeQuery();
    }
}
