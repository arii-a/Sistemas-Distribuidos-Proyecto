package edu.upb.tickmaster.httpserver.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.tickmaster.httpserver.repositories.EventRepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class EventPostHandler implements HttpHandler {

    private static final EventRepository repository = new EventRepository();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int statusCode = 200;

        try {
            Headers responseHeaders = exchange.getResponseHeaders();
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

                        if (!jsonBody.has("nombre") || !jsonBody.has("fecha")
                                || !jsonBody.has("capacidad")) {
                            response = "{\"status\": \"ERROR\", \"message\": \"Campos incompletos\"}";
                            statusCode = 400;
                        } else {
                            String nombre = jsonBody.get("nombre").getAsString().trim();
                            String fechaStr = jsonBody.get("fecha").getAsString().trim();
                            int capacidad = jsonBody.get("capacidad").getAsInt();

                            java.sql.Date fecha = null;
                            try {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                java.util.Date utilDate = sdf.parse(fechaStr);
                                fecha = new java.sql.Date(utilDate.getTime());
                            } catch (ParseException e) {
                                e.printStackTrace();
                            }

                            int[] result = repository.insertClient(nombre, fecha, capacidad);
                            statusCode = result[0];

                            if (statusCode == 201) {
                                response = "{\"status\": \"OK\", \"message\": \"Evento creado exitosamente\"}";
                            } else {
                                response = "{\"status\": \"ERROR\", \"message\": \"No se pudo crear el evento " + statusCode + "\"}";
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

}