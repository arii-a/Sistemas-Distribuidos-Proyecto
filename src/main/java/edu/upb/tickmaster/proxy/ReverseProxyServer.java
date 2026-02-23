package edu.upb.tickmaster.proxy;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ReverseProxyServer {
    private static final Logger logger = LoggerFactory.getLogger(ReverseProxyServer.class);
    private HttpServer server;
    private final int port;
    private final List<BackendInstance> backends;
    private final AtomicInteger currentIndex;
    private final LoadBalancingStrategy strategy;

    public enum LoadBalancingStrategy {
        ROUND_ROBIN,
        RANDOM
    }

    private final AtomicInteger counter = new AtomicInteger(0);

    public ReverseProxyServer(int port, LoadBalancingStrategy strategy) {
        this.port = port;
        this.backends = new ArrayList<>();
        this.currentIndex = new AtomicInteger(0);
        this.strategy = strategy;
    }

    public void addBackendServer(String host, int port, String instanceId) {
        backends.add(new BackendInstance(port, instanceId));
        logger.info("Added backend: " + instanceId + " at " + host + ":" + port);
    }

    public boolean start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", this::handleRequest);
            server.setExecutor(Executors.newFixedThreadPool(20));
            server.start();
            logger.info("\nProxy on port " + port);
            System.out.println("Backends: " + backends);
            return true;
        } catch (IOException e) {
            System.err.println("Proxy error: " + e.getMessage());
            logger.error("Proxy error: " + e.getMessage());
            return false;
        }
    }

    private void handleRequest(HttpExchange ex) throws IOException {
        byte[] requestBody = ex.getRequestBody().readAllBytes();

        if (ex.getRequestMethod().equals("POST")) {
            String body = new String(requestBody, StandardCharsets.UTF_8);
            try {
                JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                if (!json.has("idempotency_key")) {
                    json.addProperty("idempotency_key", UUID.randomUUID().toString());
                }
                requestBody = json.toString().getBytes(StandardCharsets.UTF_8);
            } catch (Exception ignored) {}
        }

        int maxRetries = 3;

        //Set<String> failedBackends = new HashSet<>();

        for (int i = 0; i < maxRetries; i++) {
            BackendInstance backend = selectBackend();

            HttpURLConnection conn = null;
            try {
                if (backend == null) {
                    sendErrorResponse(ex, 503, "No backend servers available");
                    logger.error("No backend servers available");
                    return;
                }

                logger.info("Intento " + (i + 1) + " - Forwarding " + ex.getRequestMethod() +
                        " " + ex.getRequestURI() + " to " + backend.instanceId);

                String path = ex.getRequestURI().toString();
                String targetUrl = "http://localhost:" + backend.port + path;

                conn = (HttpURLConnection) new URL(targetUrl).openConnection();
                conn.setRequestMethod(ex.getRequestMethod());
                conn.setConnectTimeout(3);
                conn.setReadTimeout(2000);
                conn.setDoInput(true);
                conn.setDoOutput(true);

                HttpURLConnection finalConn = conn;
                ex.getRequestHeaders().forEach((key, values) -> {
                    if (!key.equalsIgnoreCase("Host") && !key.equalsIgnoreCase("Connection")) {
                        values.forEach(v -> finalConn.setRequestProperty(key, v));
                    }
                });

                if (ex.getRequestMethod().equals("POST") && requestBody.length > 0) {
                    try (OutputStream out = conn.getOutputStream()) {
                        out.write(requestBody);
                        out.flush();
                    }
                }

                int status = conn.getResponseCode();

                InputStream responseStream;
                try {
                    responseStream = (status < 400) ? conn.getInputStream() : conn.getErrorStream();
                } catch (IOException e) {
                    logger.error("Error getting response stream: " + e.getMessage());
                    throw e;
                }

                if (responseStream == null) {
                    logger.warn("Response stream is null, reintentando...");
                    throw new IOException("No response from backend");
                }

                ByteArrayOutputStream responseBuffer = new ByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = responseStream.read(buffer)) != -1) {
                    responseBuffer.write(buffer, 0, bytesRead);
                }
                responseStream.close();
                byte[] responseBody = responseBuffer.toByteArray();

                conn.getHeaderFields().forEach((key, values) -> {
                    if (key != null && !key.equalsIgnoreCase("Transfer-Encoding")) {
                        values.forEach(v -> ex.getResponseHeaders().add(key, v));
                    }
                });

                ex.getResponseHeaders().add("Proxy-Backend", backend.instanceId);
                ex.sendResponseHeaders(status, responseBody.length);

                try (OutputStream clientOutput = ex.getResponseBody()) {
                    clientOutput.write(responseBody);
                    clientOutput.flush();
                }

                return;

            } catch (Exception e) {
                //failedBackends.add(backend.instanceId);
                //logger.info("Agregando Backend " + backend.instanceId + " a failed backends.");
                logger.warn("Intento " + (i + 1) + " fallido: " + e.getMessage());
                if (conn != null) conn.disconnect();

                if (i < maxRetries - 1) {
                    try { Thread.sleep(1000 * (long) Math.pow(2, i)); } catch (InterruptedException ignored) {}
                }
            }
        }

        logger.error("Todos los intentos fallaron");
        sendErrorResponse(ex, 502, "Bad Gateway");
        ex.getResponseBody().close();
    }

    private BackendInstance selectBackend() { //Set<String> failedBackends) {
        /*List<BackendInstance> availableBackends = backends.stream().
                filter(x -> !failedBackends.contains(x.instanceId))
                .collect(Collectors.toList());*/
        if (backends.isEmpty()) return null;
        //if (availableBackends.isEmpty()) return null;

        switch (strategy) {
            case ROUND_ROBIN:
                int index = currentIndex.getAndIncrement() % backends.size();
                return backends.get(index);
            case RANDOM:
                int randomIndex = (int) (Math.random() * backends.size());
                return backends.get(randomIndex);
            default:
                return backends.get(0);
        }
    }

    private void sendErrorResponse(HttpExchange exchange, int statusCode, String message) {
        try {
            String jsonResponse = String.format(
                    "{\"status\":\"ERROR\",\"message\":\"%s\"}",
                    message
            );
            byte[] bytes = jsonResponse.getBytes();
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        if (server != null) {
            server.stop(0);
            logger.warn("Proxy stopped");
        }
    }

    private static class BackendInstance {
        final int port;
        final String instanceId;

        BackendInstance(int port, String instanceId) {
            this.port = port;
            this.instanceId = instanceId;
        }
    }
}