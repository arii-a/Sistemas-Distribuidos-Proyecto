package edu.upb.tickmaster.httpserver.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.tickmaster.db.ConnectionDB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;
import java.util.UUID;

public class FacturaPostHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(FacturaPostHandler.class);
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
            responseHeaders.add("Content-Type", "application/json");

            String method = exchange.getRequestMethod();

            if (method.equals("POST")) {
                try (InputStream is = exchange.getRequestBody()) {
                    Scanner scanner = new Scanner(is, StandardCharsets.UTF_8.name());
                    String body = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

                    if (body == null || body.trim().isEmpty()) {
                        response = "{\"status\": \"ERROR\", \"message\": \"Cuerpo vacío\"}";
                        statusCode = 400;
                    } else {
                        JsonObject jsonBody = JsonParser.parseString(body).getAsJsonObject();

                        if (!jsonBody.has("user_id") || !jsonBody.has("monto") ||
                                !jsonBody.has("fecha") || !jsonBody.has("nit")) {
                            response = "{\"status\": \"ERROR\", \"message\": \"Campos incompletos\"}";
                            statusCode = 400;
                        } else {
                            int user = jsonBody.get("user_id").getAsInt();
                            int monto = jsonBody.get("monto").getAsInt();
                            String fechaStr = jsonBody.get("fecha").getAsString().trim();
                            int nit = jsonBody.get("nit").getAsInt();
                            String idempotencyKey = jsonBody.has("idempotency_key")
                                    ? jsonBody.get("idempotency_key").getAsString()
                                    : UUID.randomUUID().toString();

                            Date fecha = null;
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                                fecha = (Date) sdf.parse(fechaStr);
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            int[] result = insertFactura(user, monto, fecha, nit, idempotencyKey);
                            statusCode = result[0];

                            if (statusCode == 201) {
                                response = "{\"status\": \"OK\", \"message\": \"Factura creado exitosamente\"}";
                            } else if (statusCode == 200) {
                                response = "{\"status\": \"OK\", \"message\": \"Solicitud ya procesada anteriormente\"}";
                            } else {
                                response = "{\"status\": \"ERROR\", \"message\": \"No se pudo crear la factura tras varios intentos" + statusCode + "\"}";
                                statusCode = 500;
                            }
                        }
                    }
                }
            } else if (method.equals("OPTIONS")) {
                response = "{\"status\": \"OK\"}";
                statusCode = 200;

            } else {
                response = "{\"status\": \"NOK\", \"message\": \"Método no soportado\"}";
                statusCode = 405;
            }

        } catch (JsonSyntaxException e) {
            response = "{\"status\": \"ERROR\", \"message\": \"JSON inválido\"}";
            statusCode = 400;

        } catch (Exception e) {
            e.printStackTrace();
            response = "{\"status\": \"ERROR\", \"message\": \"Error interno del servidor\"}";
            statusCode = 500;
        }

        byte[] byteResponse = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, byteResponse.length);
        OutputStream os = exchange.getResponseBody();
        os.write(byteResponse);
        os.close();
    }

    private int[] insertFactura(int user, int monto, Date fecha, int nit, String idempotencyKey) {
        try {
            logger.info("Insertando factura");

            String query = "INSERT INTO public.factura (user, monto, fecha, nit, idempotency_key) VALUES (?, ?, ?, ?, ?)";
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, user);
            statement.setInt(2, monto);
            statement.setTimestamp(3, new java.sql.Timestamp(fecha.getTime()));
            statement.setInt(4, nit);
            statement.setString(5, idempotencyKey);

            int rows = statement.executeUpdate();
             /*if ("backend-1".equals(instanceId)) {
                 Thread.sleep(3000);
             }*/
            //Thread.sleep(2000); //probando funcionamiento del read timeout
            statement.close();

            if (rows > 0) {
                logger.info("Factura creado exitosamente en intento.");
                return new int[]{201};
            }

        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                logger.info("Solicitud duplicada detectada, ya fue procesada.");
                return new int[]{200};
            }
            logger.warn("Intento fallido: " + e.getMessage());
        } /*catch (InterruptedException e) {
             throw new RuntimeException(e); //solo usado para sleep
         }*/

        return new int[]{500};
    }

}