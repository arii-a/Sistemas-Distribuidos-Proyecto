package edu.upb.tickmaster.httpserver.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.tickmaster.httpserver.services.LoginService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class LoginHandler implements HttpHandler {
    private static final Logger logger = LoggerFactory.getLogger(LoginHandler.class);
    private final LoginService service = new LoginService();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int statusCode = 200;

        exchange.getResponseHeaders().add("Content-Type", "application/json");

        String method = exchange.getRequestMethod();

        if (!"POST".equals(method)) {
            response = "{\"status\": \"NOK\", \"message\": \"Método no soportado\"}";
            statusCode = 405;
        } else {
            try {
                Scanner scanner = new Scanner(exchange.getRequestBody(), StandardCharsets.UTF_8.name());
                String body = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                String username = json.get("username").getAsString();
                String password = json.get("password").getAsString();

                JsonObject result = service.login(username, password);

                if ("OK".equals(result.get("status").getAsString())) {
                    response = result.toString();
                    statusCode = 200;
                } else {
                    response = "{\"status\": \"ERROR\", \"message\": \"Credenciales incorrectas\"}";
                    statusCode = 401;
                }

        } catch (Exception e) {
                logger.error("Error en login: " + e.getMessage());
                response = "{\"status\": \"ERROR\", \"message\": \"Error interno\"}";
                statusCode = 500;
            }
        }

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}