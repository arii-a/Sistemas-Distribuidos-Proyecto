package edu.upb.tickmaster.httpserver.handlers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.tickmaster.db.ConnectionDB;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.*;

public class SectorGetHandler implements HttpHandler {

    private Connection getConnection() {
        return ConnectionDB.instance().getConnection();
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int statusCode = 200;

        try {
            exchange.getResponseHeaders().add("Content-Type", "application/json");

            if ("GET".equals(exchange.getRequestMethod())) {
                String query = exchange.getRequestURI().getQuery();
                int eventoId = -1;
                if (query != null && query.contains("evento_id=")) {
                    eventoId = Integer.parseInt(query.split("evento_id=")[1]);
                }

                response = getSectores(eventoId);
                statusCode = 200;
            } else {
                response = "{\"status\": \"NOK\", \"message\": \"Método no soportado\"}";
                statusCode = 405;
            }

        } catch (Exception e) {
            e.printStackTrace();
            response = "{\"status\": \"ERROR\", \"message\": \"Error interno\"}";
            statusCode = 500;
        }

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }

    private String getSectores(int eventoId) throws SQLException {
        JsonArray jsonArray = new JsonArray();
        String sql = eventoId > 0
                ? "SELECT id, descripción, precio, capacidad FROM public.sectores WHERE evento_id = ?"
                : "SELECT id, descripción, precio, capacidad FROM public.sectores";

        Connection conn = getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        if (eventoId > 0) stmt.setInt(1, eventoId);

        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", rs.getInt("id"));
            obj.addProperty("descripción", rs.getString("descripción"));
            obj.addProperty("precio", rs.getDouble("precio"));
            obj.addProperty("capacidad", rs.getInt("capacidad"));
            jsonArray.add(obj);
        }

        rs.close();
        stmt.close();
        return jsonArray.toString();
    }
}