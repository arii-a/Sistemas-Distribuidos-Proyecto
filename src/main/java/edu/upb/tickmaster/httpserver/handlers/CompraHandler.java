package edu.upb.tickmaster.httpserver.handlers;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.tickmaster.grpc.CompraClient;
import edu.upb.tickmaster.grpc.CompraResponse;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class CompraHandler implements HttpHandler {

    private final CompraClient compraClient = new CompraClient("localhost", 8081);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("POST".equals(exchange.getRequestMethod())) {

            InputStream is = exchange.getRequestBody();
            String body = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            JsonObject json = JsonParser.parseString(body).getAsJsonObject();

            String usuario = json.get("usuario").getAsString().trim();
            int cantidad = json.get("cantidad").getAsInt();

            CompraResponse response = compraClient.comprar(usuario, cantidad);

            JsonObject respJson = new JsonObject();
            respJson.addProperty("exitoso", response.getExitoso());
            respJson.addProperty("mensaje", response.getMensaje());
            respJson.addProperty("compra_id", response.getCompraId());

            byte[] respBytes = respJson.toString().getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, respBytes.length);
            OutputStream os = exchange.getResponseBody();
            os.write(respBytes);
            os.close();

        } else {
            exchange.sendResponseHeaders(405, -1);
        }
    }
}