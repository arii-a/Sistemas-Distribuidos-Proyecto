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
        int port[] = {1914, 1915};

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        HealthChecker healthChecker = new HealthChecker(scheduler);
        healthChecker.monitoring();

        for (int i = 0; i < port.length; i++) {
            ApacheServer backend = new ApacheServer(port[i], "instanceId - " + i, healthChecker);
            backend.start();
        }
    }
}
