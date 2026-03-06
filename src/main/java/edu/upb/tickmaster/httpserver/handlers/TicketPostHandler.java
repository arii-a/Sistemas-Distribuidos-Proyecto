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
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Scanner;
import java.util.UUID;

public class TicketPostHandler implements HttpHandler {

    private static final Logger logger = LoggerFactory.getLogger(TicketPostHandler.class);

    private Connection getConnection() {
        return ConnectionDB.instance().getConnection();
    }

    public TicketPostHandler() {

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

                        if (!jsonBody.has("usuario_id") || !jsonBody.has("evento_id") ||
                                !jsonBody.has("sector_id") || !jsonBody.has("factura_id")) {
                            response = "{\"status\": \"ERROR\", \"message\": \"Campos incompletos\"}";
                            statusCode = 400;
                        } else {
                            int user = jsonBody.get("usuario_id").getAsInt();
                            int evento = jsonBody.get("evento_id").getAsInt();
                            int sector = jsonBody.get("sector_id").getAsInt();
                            int factura = jsonBody.get("factura_id").getAsInt();
                            String idempotencyKey = jsonBody.has("idempotency_key")
                                    ? jsonBody.get("idempotency_key").getAsString()
                                    : UUID.randomUUID().toString();

                            int[] result = insertTicket(user, evento, sector, factura, idempotencyKey);
                            statusCode = result[0];

                            if (statusCode == 201) {
                                response = "{\"status\": \"OK\", \"message\": \"Ticket creado exitosamente\"}";
                            } else if (statusCode == 200) {
                                response = "{\"status\": \"OK\", \"message\": \"Solicitud ya procesada anteriormente\"}";
                            } else {
                                response = "{\"status\": \"ERROR\", \"message\": \"No se pudo crear el cliente tras varios intentos" + statusCode + "\"}";
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

    private int[] insertTicket(int user, int evento, int sector, int factura, String idempotencyKey) {
        try {
            logger.info("Insertando ticket");

            String query = "INSERT INTO public.ticket_compra (user, evento, sector, factura, idempotency_key) VALUES (?, ?, ?, ?, ?)";
            Connection conn = getConnection();
            PreparedStatement statement = conn.prepareStatement(query);
            statement.setInt(1, user);
            statement.setInt(2, evento);
            statement.setInt(3, sector);
            statement.setInt(4, factura);
            statement.setString(5, idempotencyKey);

            int rows = statement.executeUpdate();
             /*if ("backend-1".equals(instanceId)) {
                 Thread.sleep(3000);
             }*/
            //Thread.sleep(2000); //probando funcionamiento del read timeout
            statement.close();

            if (rows > 0) {
                logger.info("Ticket creado exitosamente en intento.");
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
