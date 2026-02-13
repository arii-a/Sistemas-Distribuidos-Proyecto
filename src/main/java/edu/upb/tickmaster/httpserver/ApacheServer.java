/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.tickmaster.httpserver;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpServer;
import edu.upb.tickmaster.health.HealthChecker;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

/**
 *
 * @author rlaredo
 */
public class ApacheServer {
    private HttpServer server = null;
    private boolean isServerDone = false;
    private final int port;
    private final String instanceId;

    private final HealthChecker healthChecker;

    public ApacheServer(int port, String instanceId, HealthChecker healthChecker){
        this.port = port;
        this.instanceId = instanceId;
        this.healthChecker = healthChecker;
    }
    
    public boolean start() {
        try {
            this.server = HttpServer.create(new InetSocketAddress(port), 0);

            this.server.createContext("/", exchange -> {
                Headers headers = exchange.getResponseHeaders();
                headers.add("Access-Control-Allow-Origin", "*");
                headers.add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                headers.add("Access-Control-Allow-Headers", "Content-Type, Authorization");

                headers.add("X-Backend-Instance", instanceId);

                new RootHandler().handle(exchange);
            });

            this.server.createContext("/hola", exchange -> {
                exchange.getResponseHeaders().add("X-Backend-Instance", instanceId);
                new EchoPostHandler().handle(exchange);
            });

            this.server.createContext("/clients", exchange -> {
                exchange.getResponseHeaders().add("X-Backend-Instance", instanceId);
                new ClientsGetHandler().handle(exchange);
            });

            this.server.createContext("/backendHealth", exchange -> {
                String response = String.format(
                        "{\"status\":\"OK\",\"instance\":\"%s\",\"port\":%d}",
                        instanceId, port
                );
                byte[] bytes = response.getBytes();

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.getResponseHeaders().add("X-Backend-Instance", instanceId);
                exchange.sendResponseHeaders(200, bytes.length);

                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            });

            this.server.createContext("/monitorHealth", exchange -> {
                String response = String.format(
                        "{\"status\":\"%s\",\"database\":\"%s\",\"disk\":\"%s\",\"instance\":\"%s\"}",
                        healthChecker.isSystemHealthy() ? "healthy" : "unhealthy",
                        healthChecker.isDatabaseHealthy() ? "up" : "down",
                        healthChecker.isDiskHealthy() ? "ok" : "low",
                        instanceId
                );
                byte[] bytes = response.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().add("Content-Type", "application/json");
                exchange.getResponseHeaders().add("X-Backend-Instance", instanceId);
                exchange.sendResponseHeaders(200, bytes.length);

                OutputStream os = exchange.getResponseBody();
                os.write(bytes);
                os.close();
            });

            this.server.setExecutor(Executors.newFixedThreadPool(10));

            this.server.start();

            System.out.println("Backend started on " + port + " port");
            return true;

        } catch (IOException e) {
            System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
            this.server = null;
        }
        return false;
    }

        public int getPort() {
        return port;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public void stop(){
        if (this.server != null) {
            this.server.stop(0);
            this.server = null;
            System.out.println("Backend server stopped");
        }
    }

  
    
}
