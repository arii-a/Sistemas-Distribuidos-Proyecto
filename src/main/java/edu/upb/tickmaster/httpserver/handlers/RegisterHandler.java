package edu.upb.tickmaster.httpserver.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.tickmaster.proxy.ReverseProxyServer.BackendInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RegisterHandler implements HttpHandler {

    private static List<BackendInstance> listBackends;
    private static final Logger logger = LoggerFactory.getLogger(RegisterHandler.class);

    public RegisterHandler() {
    }

    public RegisterHandler(List<BackendInstance> listBackends) {
        this.listBackends = listBackends;
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

                        if (!jsonBody.has("ip") || !jsonBody.has("port") || !jsonBody.has("instanceId")) {
                            response = "{\"status\": \"ERROR\", \"message\": \"Campos 'name' y 'lastname' son requeridos\"}";
                            statusCode = 400;
                        } else {
                            String ip = jsonBody.get("ip").getAsString().trim();
                            int port = jsonBody.get("port").getAsInt();
                            String instanceId = jsonBody.get("instanceId").getAsString().trim();

                            int[] result = insertBackend(ip, port, instanceId);
                            statusCode = result[0];

                            if (statusCode == 201) {
                                response = "{\"status\": \"OK\", \"message\": \"Backend creado\"}";
                            } else {
                                response = "{\"status\": \"ERROR\", \"message\": \"No se pudo crear el backend" + statusCode + "\"}";
                                statusCode = 500;
                            }
                        }
                    }
                }
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

        private int[] insertBackend(String ip, int port, String instanceId) {
            listBackends.add(new BackendInstance(ip, port, instanceId));
            logger.info("Backend agregado a lista");
            return new int[]{201};
        }


}
