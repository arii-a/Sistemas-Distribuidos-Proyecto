package edu.upb.tickmaster.httpserver.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.tickmaster.db.ConnectionDB;
import edu.upb.tickmaster.httpserver.ContentType;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class FacturaGetHandler implements HttpHandler {
    private Connection getConnection() {
        return ConnectionDB.instance().getConnection();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int statusCode = 200;

        try {
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.add("Access-Control-Allow-Origin", "*");
            responseHeaders.add("Content-Type", ContentType.JSON.toString());

            String method = exchange.getRequestMethod();

            if (method.equals("GET")) {
                response = getAllEvents();
                statusCode = 200;

            } else if (method.equals("OPTIONS")) {
                response = "{\"status\": \"OK\",\"message\": \"OPTIONS request successful\"}";
                statusCode = 200;

            } else {
                response = "{\"status\": \"NOK\",\"message\": \"Método no soportado\"}";
                statusCode = 405;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            response = "{\"status\": \"ERROR\",\"message\": \"Error de base de datos: " +
                    e.getMessage().replace("\"", "'") + "\"}";
            statusCode = 500;

        } catch (Exception e) {
            e.printStackTrace();
            response = "{\"status\": \"ERROR\",\"message\": \"Error interno del servidor\"}";
            statusCode = 500;
        }

        byte[] byteResponse = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, byteResponse.length);

        OutputStream os = exchange.getResponseBody();
        os.write(byteResponse);
        os.close();
    }

    private String getAllEvents() throws SQLException {
        JsonArray jsonArray = new JsonArray();
        String query = "SELECT u.username, f.monto, f.fecha, f.nit" +
                "FROM public.factura AS f " +
                "INNER JOIN public.users AS u " +
                "WHERE f.user_id = u.id ";

        Connection conn = getConnection();
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = conn.prepareStatement(query);
            result = statement.executeQuery();

            while (result.next()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("username", result.getString("username"));
                obj.addProperty("monto", result.getInt("monto"));
                obj.addProperty("fecha", String.valueOf(result.getDate("fecha")));
                obj.addProperty("nit", result.getInt("nit"));
                jsonArray.add(obj);
            }
        } finally {
            if (result != null) try { result.close(); } catch (SQLException e) { }
            if (statement != null) try { statement.close(); } catch (SQLException e) { }
        }

        return jsonArray.toString();
    }
    private static class ResponseResult {
        String response;
        int statusCode;

        ResponseResult(String response, int statusCode) {
            this.response = response;
            this.statusCode = statusCode;
        }
    }
}