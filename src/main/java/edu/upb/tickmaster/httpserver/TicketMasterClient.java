package edu.upb.tickmaster.httpserver;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class TicketMasterClient {

    private final String BASE_URL = "http://localhost:8080/clients";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    public void fetchAllClients() {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .GET()
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenApply(HttpResponse::body)
                .thenAccept(System.out::println)
                .join();
    }

    public void postNewClient(String name, String lastname) {
        String jsonBody = String.format("{\"name\":\"%s\", \"lastname\":\"%s\"}", name, lastname);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    System.out.println("Status Code: " + response.statusCode());
                    System.out.println("Response: " + response.body());
                })
                .join();
    }

    public static void main(String[] args) {
        TicketMasterClient client = new TicketMasterClient();

        System.out.println("Probando POST");
        client.postNewClient("Ricardo", "Laredo3");

        System.out.println("\nProbando GET");
        client.fetchAllClients();
    }
}