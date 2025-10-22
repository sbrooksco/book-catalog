package org.example;

import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.db.DataSourceFactory;
import org.example.core.Book;
import org.example.db.BookDAO;
import org.example.health.DatabaseHealthCheck;
import org.example.resources.BookResource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.postgresql.ds.PGSimpleDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BookCatalogApplication extends Application<BookCatalogConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookCatalogApplication.class);

    private final HibernateBundle<BookCatalogConfiguration> hibernate = new HibernateBundle<>(Book.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(BookCatalogConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public void initialize(io.dropwizard.core.setup.Bootstrap<BookCatalogConfiguration> bootstrap) {
        // Enable environment variable substitution in config.yaml
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(BookCatalogConfiguration configuration, Environment environment) throws Exception {
        final BookDAO dao = new BookDAO(hibernate.getSessionFactory());

        // Read database config
        String dbUrl = configuration.getDataSourceFactory().getUrl();
        String dbUser = configuration.getDataSourceFactory().getUser();
        String dbPass = configuration.getDataSourceFactory().getPassword();

        // Register the database health check.
        DatabaseHealthCheck healthCheck = new DatabaseHealthCheck(dbUrl, dbUser, dbPass);
        environment.healthChecks().register("database", healthCheck);

        LOGGER.info("Database URL: {}", dbUrl);
        LOGGER.info("Checking database readiness...");

        // --- Wait for Postgres to be ready ---
        int retries = 10;
        int waitSeconds = 3;
        boolean dbReady = false;

        for (int i = 0; i < retries; i++) {
            try (Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPass)) {
                LOGGER.info("Database is ready!");
                dbReady = true;
                break;
            } catch (SQLException e) {
                LOGGER.warn("Database not ready (attempt {}/{}). Waiting {}s...", i + 1, retries, waitSeconds);
                try {
                    Thread.sleep(waitSeconds * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        if (!dbReady) {
            throw new IllegalStateException("Unable to connect to the database after " + retries + " attempts");
        }

        // Register PostgreSQL driver explicitly
        Class.forName("org.postgresql.Driver");

        // Create a DataSource manually
        PGSimpleDataSource ds = new PGSimpleDataSource();
        ds.setUrl(dbUrl);
        ds.setUser(dbUser);
        ds.setPassword(dbPass);

        // Run Flyway migrations
        Flyway flyway = Flyway.configure()
                .dataSource(ds)
                .locations("classpath:db") // make sure your SQL migrations are in src/main/resources/db
                .baselineOnMigrate(true) // If the schema isn’t empty but has no schema history table, create one and assume it’s already at version 1.
                .load();

        try {
            LOGGER.info("Starting Flyway migrations...");
            MigrateResult result = flyway.migrate();
            LOGGER.info("Flyway migration complete. {} migrations executed.", result.migrationsExecuted);
        } catch (Exception e) {
            LOGGER.error("Flyway migration failed: {}", e.getMessage(), e);
            throw e; // Stop startup on failure
        }

        // Register your resources
        environment.jersey().register(new BookResource(dao));
        LOGGER.info("BookCatalog application started successfully!");
    }

    @Override
    public String getName() {
        return "book-catalog";
    }

    public static void main(String[] args) throws Exception {
        new BookCatalogApplication().run(args);
    }
}

