package edu.upb.tickmaster.httpserver.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.tickmaster.db.ConnectionDB;
import edu.upb.tickmaster.httpserver.ContentType;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.Scanner;
import java.util.UUID;

public class ClientsGetHandler implements HttpHandler {


    private Connection getConnection() {
        return ConnectionDB.instance().getConnection();
    }

    public ClientsGetHandler() {
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
                response = getAllClients();
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

    private String getAllClients() throws SQLException {
        JsonArray jsonArray = new JsonArray();
        String query = "SELECT name, lastname FROM public.clients";

        Connection conn = getConnection();
        PreparedStatement statement = null;
        ResultSet result = null;

        try {
            statement = conn.prepareStatement(query);
            result = statement.executeQuery();

            while (result.next()) {
                JsonObject obj = new JsonObject();
                obj.addProperty("name", result.getString("name"));
                obj.addProperty("lastname", result.getString("lastname"));
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