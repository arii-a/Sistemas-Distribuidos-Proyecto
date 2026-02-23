package edu.upb.tickmaster.httpserver;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TicketMasterClient {

    private final String BASE_URL = "http://localhost:8080/clients";
    private final HttpClient httpClient = HttpClient.newBuilder().connectTimeout(Duration.ofMillis(500)).build();

    private static final Logger logger = LoggerFactory.getLogger(TicketMasterClient.class);

    public void fetchAllClients() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .join();
    }

    public int postNewClient(String name, String lastname) {
        String idempotencyKey = UUID.randomUUID().toString();
        int maxRetries = 1;

        for (int i = 0; i < maxRetries; i++) {
            System.out.println("primer intento");
            try {
                String jsonBody = String.format("{\"name\":\"%s\", \"lastname\":\"%s\", \"idempotensy_key\":\"%s\"}",
                        name, lastname, idempotencyKey);

                System.out.println("creando request");
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(BASE_URL))
                        .timeout(Duration.ofSeconds(10))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                        .build();

                System.out.println("mandando solicitud");
                int statusCode = httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                        .thenApply(response -> {
                            logger.info("Status: " + response.statusCode());
                            logger.info("Response: " + response.body());
                            return response.statusCode();
                        })
                        .join();

                if (statusCode != 502 && statusCode != 503) {
                    System.out.println("todo bien");
                    return statusCode;
                }

                logger.warn("Intento " + (i + 1) + " fallido con " + statusCode + ", reintentando...");
                Thread.sleep(1000 * (long) Math.pow(2, i));

            } catch (Exception e) {
                System.out.println("acabó todo fallido");
                logger.warn("Intento " + (i + 1) + " fallido: " + e.getMessage());
                try { Thread.sleep(1000 * (long) Math.pow(2, i)); } catch (InterruptedException ignored) {}
            }
        }

        return 500;
    }

    public static void main(String[] args) {
        TicketMasterClient client = new TicketMasterClient();

        System.out.println("Probando POST");
        client.postNewClient("Ricardo", "Laredo3");

        System.out.println("\nProbando GET");
        client.fetchAllClients();
    }
}