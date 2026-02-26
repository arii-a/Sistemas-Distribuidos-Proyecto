package edu.upb.tickmaster;

import edu.upb.tickmaster.httpserver.ApacheServer;
import edu.upb.tickmaster.proxy.ReverseProxyServer;
import edu.upb.tickmaster.health.HealthChecker;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    public static void main(String[] args) {

        //new File("logs").mkdirs();
        File dir = new File("logs");
        if (!dir.exists()) dir.mkdirs();

        logger.info("Application starting...");

        int proxyPort = 8080;
        int[] ports = {1914, 1915};

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        HealthChecker healthChecker = new HealthChecker(scheduler);
        healthChecker.monitoring();

        ReverseProxyServer proxy = new ReverseProxyServer(
                proxyPort,
                ReverseProxyServer.LoadBalancingStrategy.ROUND_ROBIN
        );

        if (proxy.start()) {
            System.out.println("\nReverse Proxy started on port " + proxyPort);
            logger.info("\nReverse Proxy started on port " + proxyPort);
        } else {
            System.err.println("Failed to start reverse proxy");
            logger.error("Failed to start reverse proxy");
        }

        for (int i = 0; i < ports.length; i++) {
            String instanceId = "backend-" + (i + 1);
            ApacheServer backend = new ApacheServer(ports[i], instanceId, healthChecker);

            if (backend.start()) {
                System.out.println("Backend '" + instanceId + "' started on port " + ports[i]);
                logger.info("Backend '" + instanceId + "' started on port " + ports[i]);
            } else {
                System.err.println("Failed to start backend on port " + ports[i]);
                logger.error("Failed to start backend on port " + ports[i]);
                return;
            }
        }


        for (int i = 0; i < ports.length; i++) {
            proxy.addBackendServer("localhost", ports[i], "backend-" + (i + 1));
        }

        /*String instanceId = "";
        ApacheServer apacheServer = new ApacheServer(port, instanceId);
        apacheServer.start();*/
    }
}
