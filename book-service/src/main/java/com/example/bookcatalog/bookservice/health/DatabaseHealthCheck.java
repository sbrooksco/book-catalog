package com.example.bookcatalog.bookservice.health;

import com.codahale.metrics.health.HealthCheck;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHealthCheck extends HealthCheck {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPass;

    public DatabaseHealthCheck(String dbUrl, String dbUser, String dbPass) {
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPass = dbPass;
    }

    @Override
    protected Result check() throws Exception {
        try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
            if (conn.isValid(2)) { // 2 second timeout
                return Result.healthy();
            } else {
                return Result.unhealthy("Connection is not valid");
            }
        } catch (SQLException e) {
            return Result.unhealthy("Cannot connect to database: " + e.getMessage());
        }
    }
}
