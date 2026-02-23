package edu.upb.tickmaster.health;

import edu.upb.tickmaster.db.ConnectionDB;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.io.File;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HealthChecker {

    private final ScheduledExecutorService scheduler;
    // this allows scheduling of tasks to execute after a given delay or at fixed intervals

    private volatile boolean isDatabaseHealthy = false;
    private volatile boolean isDiskHealthy = false;

    private static final Logger logger = LoggerFactory.getLogger(HealthChecker.class);


    public HealthChecker(ScheduledExecutorService scheduler) {
        this.scheduler = scheduler;
    }

    public static void main(String[] args) {

    }

    public void monitoring () {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                isDatabaseHealthy = connectionIsAlive();
                logger.info("Database: " +
                        (isDatabaseHealthy ? "UP" : "DOWN"));
            } catch (SQLException e) {
                isDatabaseHealthy = false;
                logger.error("Database check failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }, 0, 30, TimeUnit.SECONDS);

        scheduler.scheduleAtFixedRate(() -> {
            isDiskHealthy = diskSpace();
            logger.info("Disk: " +
                    (isDiskHealthy ? "OK" : "LOW SPACE"));
        }, 0, 60, TimeUnit.SECONDS);

        logger.info("Health monitoring started");
    }

    private Connection getConnection() {
        return ConnectionDB.instance().getConnection();
    }

    // checks db connection
    private boolean connectionIsAlive() throws SQLException {
        try {
            Connection conn = getConnection();

            if (conn == null) {
                logger.error("Connection is NULL!");
                throw new SQLException("Connection is null");
            }

            //System.out.println("Testing connection with isValid()...");

            if (conn.isValid(5)) {
                logger.info("Database connection is valid");
                return true;
            } else {
                throw new SQLException("Database connection not valid");
            }
        } catch (SQLException e){
            logger.error("Database health check failed: " + e.getMessage());
            e.printStackTrace();

            throw e;
        }
    }

    // checks disk space
    private boolean diskSpace() {
        File disk = new File("C:\\"); // root directory
        long freeSpace = disk.getUsableSpace();
        long totalSpace = disk.getTotalSpace();

        double spaceDisk = (freeSpace * 100.0) / totalSpace;

        if(spaceDisk < 20 && spaceDisk > 10) {
            System.out.println("Warning");
        }

        return spaceDisk > 20.0;
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    public boolean isDatabaseHealthy() {
        return isDatabaseHealthy;
    }

    public boolean isDiskHealthy() {
        return isDiskHealthy;
    }

    public boolean isSystemHealthy() {
        return isDatabaseHealthy && isDiskHealthy;
    }
}
