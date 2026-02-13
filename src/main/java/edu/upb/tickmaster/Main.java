package edu.upb.tickmaster;

import edu.upb.tickmaster.httpserver.ApacheServer;
import edu.upb.tickmaster.proxy.ReverseProxyServer;
import edu.upb.tickmaster.health.HealthChecker;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class Main {
    public static void main(String[] args) {
        int proxyPort = 8080;
        int[] ports = {1914, 1915};

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        HealthChecker healthChecker = new HealthChecker(scheduler);
        healthChecker.monitoring();

        for (int i = 0; i < ports.length; i++) {
            String instanceId = "backend-" + (i + 1);
            ApacheServer backend = new ApacheServer(ports[i], instanceId, healthChecker);

            if (backend.start()) {
                System.out.println("Backend '" + instanceId + "' started on port " + ports[i]);
            } else {
                System.err.println("Failed to start backend on port " + ports[i]);
                return;
            }
        }

        ReverseProxyServer proxy = new ReverseProxyServer(
                proxyPort,
                ReverseProxyServer.LoadBalancingStrategy.ROUND_ROBIN
        );


        for (int i = 0; i < ports.length; i++) {
            proxy.addBackendServer("localhost", ports[i], "backend-" + (i + 1));
        }

        if (proxy.start()) {
            System.out.println("\nReverse Proxy started on port " + proxyPort);
        } else {
            System.err.println("Failed to start reverse proxy");
        }

        /*String instanceId = "";
        ApacheServer apacheServer = new ApacheServer(port, instanceId);
        apacheServer.start();*/
    }
}
