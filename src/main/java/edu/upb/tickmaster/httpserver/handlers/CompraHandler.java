package edu.upb.tickmaster.httpserver.handlers;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import edu.upb.tickmaster.httpserver.services.CompraService;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Scanner;

public class CompraHandler implements HttpHandler {

    private static final CompraService service = new CompraService();
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String response = "";
        int statusCode = 200;

        try {
            Headers responseHeaders = exchange.getResponseHeaders();
            responseHeaders.add("Content-Type", "application/json");

            if ("POST".equals(exchange.getRequestMethod())) {
                Scanner scanner = new Scanner(exchange.getRequestBody(), StandardCharsets.UTF_8.name());
                String body = scanner.useDelimiter("\\A").hasNext() ? scanner.next() : "";

                if (body == null || body.trim().isEmpty()) {
                    response = "{\"status\": \"ERROR\", \"message\": \"Cuerpo vacío\"}";
                    statusCode = 400;
                } else {
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();

                    // Validar campos requeridos
                    if (!json.has("usuario_id") || !json.has("monto") ||
                            !json.has("nit") || !json.has("fecha") ||
                            !json.has("evento_id") || !json.has("sector_id") ||
                            !json.has("cantidad")) {
                        response = "{\"status\": \"ERROR\", \"message\": \"Campos incompletos\"}";
                        statusCode = 400;
                    } else {
                        int usuarioId = json.get("usuario_id").getAsInt();
                        int monto = json.get("monto").getAsInt();
                        int nit = json.get("nit").getAsInt();
                        int eventoId = json.get("evento_id").getAsInt();
                        int sectorId = json.get("sector_id").getAsInt();
                        int cantidad = json.get("cantidad").getAsInt();
                        String fechaStr = json.get("fecha").getAsString().trim();
                        String idempotencyKey = json.has("idempotency_key")
                                ? json.get("idempotency_key").getAsString()
                                : null;

                        java.util.Date fecha = null;
                        try {
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
                            sdf.setLenient(false);
                            fecha = sdf.parse(fechaStr);
                        } catch (ParseException e) {
                            response = "{\"status\": \"ERROR\", \"message\": \"Fecha inválida\"}";
                            statusCode = 400;
                        }

                        if (fecha != null) {
                            int[] result = service.procesarCompra(
                                    usuarioId, monto, fecha, nit,
                                    eventoId, sectorId, cantidad, idempotencyKey);
                            statusCode = result[0];

                            if (statusCode == 201) {
                                response = "{\"status\": \"OK\", \"message\": \"Compra registrada exitosamente\", \"factura_id\": " + result[1] + "}";
                            } else if (statusCode == 200) {
                                response = "{\"status\": \"OK\", \"message\": \"Solicitud ya procesada anteriormente\"}";
                            } else {
                                response = "{\"status\": \"ERROR\", \"message\": \"Error al procesar la compra\"}";
                                statusCode = 500;
                            }
                        }
                    }
                }

            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
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

        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(bytes);
        os.close();
    }
}
