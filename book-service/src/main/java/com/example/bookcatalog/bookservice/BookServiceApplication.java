package com.example.bookcatalog.bookservice;

import com.example.bookcatalog.bookservice.resources.MetricsResource;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.db.DataSourceFactory;
import com.example.bookcatalog.bookservice.core.Book;
import com.example.bookcatalog.bookservice.db.BookDAO;
import com.example.bookcatalog.bookservice.health.DatabaseHealthCheck;
import com.example.bookcatalog.bookservice.resources.BookResource;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.dropwizard.DropwizardExports;
import io.prometheus.client.exporter.MetricsServlet;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.output.MigrateResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BookServiceApplication extends Application<BookServiceConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookServiceApplication.class);

    private final HibernateBundle<BookServiceConfiguration> hibernate = new HibernateBundle<>(Book.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(BookServiceConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    /**
     * Initializes the application.
     *
     * This method is called by the Dropwizard framework once the application is started.
     * It enables environment variable substitution in the configuration file and adds the Hibernate bundle
     * to the application.
     *
     * @param bootstrap the Bootstrap object used to configure the application
     */
    @Override
    public void initialize(io.dropwizard.core.setup.Bootstrap<BookServiceConfiguration> bootstrap) {
        // Enable environment variable substitution in config.yaml
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
        bootstrap.addBundle(hibernate);
    }

    /**
     * Start the application.
     *
     * This method is called by the Dropwizard framework once the application is started.
     * It creates a BookDAO using the Hibernate bundle, runs Flyway migrations on the database, and registers the
     * BookResource with the Jersey client and Book Service URL.
     *
     * @param configuration the configuration object used to configure the application
     * @param environment the environment object used to register resources and health checks
     * @throws Exception if the application encounters an error while starting
     */
    @Override
    public void run(BookServiceConfiguration configuration, Environment environment) throws Exception {
        final BookDAO dao = new BookDAO(hibernate.getSessionFactory());

        // Add CORS filter
        configureCors(environment);

        // Add JWT authentication filter.  COMMENT THIS OUT TO TEST WITHOUT AUTH TESTING (LOCALLY)
        environment.servlets().addFilter("JwtAuth", new com.example.bookcatalog.bookservice.auth.JwtAuthFilter(configuration.getClerkDomain()))
                .addMappingForUrlPatterns(java.util.EnumSet.allOf(jakarta.servlet.DispatcherType.class), true, "/*");

        // Read database config
        String dbUrl = configuration.getDataSourceFactory().getUrl();
        String dbUser = configuration.getDataSourceFactory().getUser();
        String dbPass = configuration.getDataSourceFactory().getPassword();

        // Register the database health check.
        DatabaseHealthCheck healthCheck = new DatabaseHealthCheck(dbUrl, dbUser, dbPass);
        environment.healthChecks().register("database", healthCheck);

        // Expose Prometheus metrics on the application port.
        new io.prometheus.client.dropwizard.DropwizardExports(environment.metrics()).register();
        environment.jersey().register(new MetricsResource());

        LOGGER.info("Database URL: {}", dbUrl);

        // Register PostgreSQL driver explicitly
        Class.forName("org.postgresql.Driver");

        // Run Flyway migrations in dedicated schema
        Flyway flyway = Flyway.configure()
                .dataSource(dbUrl, dbUser, dbPass)
                .schemas("books_schema") // Dedicated schema for book-service
                .locations("classpath:db")
                .table("flyway_schema_history")
                .createSchemas(true) // Auto-create schema if it doesn't exist
                .baselineOnMigrate(true)
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

    /**
     * Returns the name of the application.
     *
     * This method is overridden from Application and it returns the name of the application.
     * The name is used in the Dropwizard banner to identify the application.
     *
     * @return the name of the application
     */
    @Override
    public String getName() {
        return "book-service";
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) for the application.
     *
     * This method is called once the application is started and it sets up CORS
     * to allow requests from any origin, with any of the standard HTTP headers,
     * and with any of the standard HTTP methods.
     *
     * @param environment the environment object used to register resources and health checks
     */
    private void configureCors(Environment environment) {
        final var cors = environment.servlets().addFilter("CORS", org.eclipse.jetty.servlets.CrossOriginFilter.class);
        cors.addMappingForUrlPatterns(java.util.EnumSet.allOf(jakarta.servlet.DispatcherType.class), true, "/*");
        cors.setInitParameter(org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        cors.setInitParameter(org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
        cors.setInitParameter(org.eclipse.jetty.servlets.CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
    }

    public static void main(String[] args) throws Exception {
        new BookServiceApplication().run(args);
    }
}

