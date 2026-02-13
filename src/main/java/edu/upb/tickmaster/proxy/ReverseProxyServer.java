package edu.upb.tickmaster.proxy;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import edu.upb.tickmaster.httpserver.ApacheServer;


import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ReverseProxyServer {
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
        System.out.println("Added backend: " + instanceId + " at " + host + ":" + port);
    }

    public boolean start() {
        try {
            server = HttpServer.create(new InetSocketAddress(port), 0);

            server.createContext("/", this::handleRequest);

            server.setExecutor(Executors.newFixedThreadPool(20));
            server.start();

            System.out.println("\nProxy on port " + port);
            System.out.println("Backends: " + backends);
            return true;
        } catch (IOException e) {
            System.err.println("Proxy error: " + e.getMessage());
            return false;
        }
    }

    private void handleRequest(HttpExchange ex) throws IOException {
        BackendInstance backend = null;
        HttpURLConnection conn = null;

        try {
            backend = selectBackend();

            if (backend == null) {
                sendErrorResponse(ex, 503, "No backend servers available");
                return;
            }

            String path = ex.getRequestURI().toString();
            String targetUrl = "http://localhost:" + backend.port + path;

            System.out.println("Forwarding " + ex.getRequestMethod() + " " + path +
                    " to " + backend.instanceId);

            conn = (HttpURLConnection) new URL(targetUrl).openConnection();
            conn.setRequestMethod(ex.getRequestMethod());
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);


            HttpURLConnection finalConn = conn;
            ex.getRequestHeaders().forEach((key, values) -> {
                if (!key.equalsIgnoreCase("Host") && !key.equalsIgnoreCase("Connection")) {
                    values.forEach(v -> finalConn.setRequestProperty(key, v));
                }
            });

            if (ex.getRequestMethod().equals("POST")) {
                try (InputStream in = ex.getRequestBody();
                     OutputStream out = conn.getOutputStream()) {
                    byte[] buffer = new byte[8192];
                    int bytesRead;
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                    }
                    out.flush();
                }
            }

            int status = conn.getResponseCode();

            InputStream responseStream;
            try {
                responseStream = (status < 400) ?
                        conn.getInputStream() : conn.getErrorStream();
            } catch (IOException e) {
                System.err.println("Error getting response stream: " + e.getMessage());
                sendErrorResponse(ex, 502, "Backend error: " + e.getMessage());
                return;
            }

            if (responseStream == null) {
                System.err.println("Response stream is null");
                sendErrorResponse(ex, 502, "No response from backend");
                return;
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

        } catch (Exception e) {
            System.err.println("Proxy error: " + e.getMessage());
            String error = "{\"error\":\"" + e.getMessage() + "\"}";
            ex.sendResponseHeaders(502, error.length());
            ex.getResponseBody().write(error.getBytes());
        } finally {
            if (conn != null) conn.disconnect();
            ex.getResponseBody().close();
        }
    }

    private BackendInstance selectBackend() {
        if (backends.isEmpty()) {
            return null;
        }

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
            System.out.println("Proxy stopped");
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