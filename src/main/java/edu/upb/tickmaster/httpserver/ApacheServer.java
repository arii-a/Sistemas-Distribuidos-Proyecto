/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package edu.upb.tickmaster.httpserver;


import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edu.upb.tickmaster.Main;
import edu.upb.tickmaster.health.HealthChecker;
import edu.upb.tickmaster.httpserver.handlers.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

/**
 *
 * @author rlaredo
 */
public class ApacheServer {
    private HttpServer server = null;
    private boolean isServerDone = false;
    private int port;
    private String instanceId;

    private final HealthChecker healthChecker;

    private static final Logger logger = LoggerFactory.getLogger(ApacheServer.class);

    Properties props = new Properties();

    public ApacheServer(int port, String instanceId, HealthChecker healthChecker){
        this.port = port;
        this.instanceId = instanceId;
        this.healthChecker = healthChecker;
    }
    
    public boolean start() {
        try {
            props.load(ApacheServer.class.getClassLoader().getResourceAsStream("paths.properties"));

            this.server = HttpServer.create(new InetSocketAddress(port), 0);

            for (String path : props.stringPropertyNames()) {
                String className = props.getProperty(path);
                try {
                    Class<?> clase = Class.forName(className);
                    HttpHandler handler = (HttpHandler) clase.getDeclaredConstructor().newInstance();
                    this.server.createContext(path, exchange -> {
                        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
                        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");
                        exchange.getResponseHeaders().add("X-Backend-Instance", instanceId);
                        handler.handle(exchange);
                    });
                    logger.info("Registered: " + path + " -> " + className);
                } catch (Exception e) {
                    logger.error("Failed to load handler for " + path + ": " + e.getMessage());
                }
            }

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

            selfRegister();

            //System.out.println("Backend started on " + port + " port");
            return true;

        } catch (IOException e) {
            //System.err.println("Error al iniciar el servidor: " + e.getMessage());
            e.printStackTrace();
            this.server = null;
        }
        return false;
    }

    private void selfRegister() {
        try {
            String jsonBody = String.format(
                    "{\"ip\":\"localhost\",\"port\":%d,\"instanceId\":\"%s\"}",
                    port, instanceId
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:8080/register"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Backend " + instanceId + " registrado exitosamente");

        } catch (Exception e) {
            logger.error("Error al registrarse: " + e.getMessage());
        }
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
            //System.out.println("Backend server stopped");
        }
    }

  
    
}
