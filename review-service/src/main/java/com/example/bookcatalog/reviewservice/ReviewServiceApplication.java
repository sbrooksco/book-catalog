package com.example.bookcatalog.reviewservice;

import com.example.bookcatalog.reviewservice.resources.MetricsResource;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.client.JerseyClientBuilder;

import com.example.bookcatalog.reviewservice.core.Review;
import com.example.bookcatalog.reviewservice.db.ReviewDAO;
import com.example.bookcatalog.reviewservice.resources.ReviewResource;

import jakarta.ws.rs.client.Client;
import org.flywaydb.core.Flyway;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReviewServiceApplication extends Application<ReviewServiceConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceApplication.class);

    /**
     * Entry point for the Review Service application.
     *
     * This method is called to start the Review Service application.
     *
     * @param args the command line arguments passed to the application
     * @throws Exception if the application encounters an error while starting
     */
    public static void main(String[] args) throws Exception {
        new ReviewServiceApplication().run(args);
    }

    /**
     * The Hibernate bundle for the Review entity.
     *
     * This bundle is used to configure the Hibernate bundle for the Review entity.
     *
     * @param configuration the configuration object used to configure the application
     * @return the Hibernate bundle for the Review entity
     */
    private final HibernateBundle<ReviewServiceConfiguration> hibernateBundle =
            new HibernateBundle<>(Review.class) {
                @Override
                public DataSourceFactory getDataSourceFactory(ReviewServiceConfiguration configuration) {
                    return configuration.getDataSourceFactory();
                }
            };

    /**
     * Initialize the application.
     *
     * This method is called by the Dropwizard framework once the application is started.
     * It enables environment variable substitution in the configuration file and adds the Hibernate bundle
     * to the application.
     *
     * @param bootstrap the Bootstrap object used to configure the application
     */
    @Override
    public void initialize(Bootstrap<ReviewServiceConfiguration> bootstrap) {
        // Enable environment variable substitution in config.yaml
        bootstrap.setConfigurationSourceProvider(
                new SubstitutingSourceProvider(
                        bootstrap.getConfigurationSourceProvider(),
                        new EnvironmentVariableSubstitutor(false)
                )
        );
        bootstrap.addBundle(hibernateBundle);
    }

    /**
     * Start the application.
     *
     * This method is called by the Dropwizard framework once the application is started.
     * It creates a ReviewDAO using the Hibernate bundle, runs Flyway migrations on the database,
     * and registers the ReviewResource with the Jersey client and Book Service URL.
     *
     * @param configuration the configuration object used to configure the application
     * @param environment the environment object used to register resources and health checks
     */
    @Override
    public void run(ReviewServiceConfiguration configuration, Environment environment) {
        final ReviewDAO reviewDAO = new ReviewDAO(hibernateBundle.getSessionFactory());

        // Add CORS filter
        configureCors(environment);

        // Add JWT authentication filter
        environment.servlets().addFilter("JwtAuth", new com.example.bookcatalog.reviewservice.auth.JwtAuthFilter(configuration.getClerkDomain()))
                .addMappingForUrlPatterns(java.util.EnumSet.allOf(jakarta.servlet.DispatcherType.class), true, "/*");

        // Expose Prometheus metrics on the application port.
        new io.prometheus.client.dropwizard.DropwizardExports(environment.metrics()).register();
        environment.jersey().register(new MetricsResource());

        // Get database config
        DataSourceFactory dsf = configuration.getDataSourceFactory();

        // Run Flyway migrations
        Flyway flyway = Flyway.configure()
                .dataSource(dsf.getUrl(), dsf.getUser(), dsf.getPassword())
                .schemas("reviews_schema")
                .locations("classpath:db")
                .table("flyway_schema_history_reviews") // Separate table for review-service
                .baselineOnMigrate(true)
                .load();

        LOGGER.info("Starting Flyway migrations...");
        flyway.migrate();
        LOGGER.info("Flyway migrations complete");

        // Create a Jersey client to call Book Service
        final Client client = new JerseyClientBuilder(environment).build("review-service-client");
        final String bookServiceUrl = configuration.getBookServiceUrl();

        // Pass the client and URL into your resource
        environment.jersey().register(new ReviewResource(reviewDAO, client, bookServiceUrl));
    }

    private void configureCors(Environment environment) {
        final var cors = environment.servlets().addFilter("CORS", org.eclipse.jetty.servlets.CrossOriginFilter.class);
        cors.addMappingForUrlPatterns(java.util.EnumSet.allOf(jakarta.servlet.DispatcherType.class), true, "/*");
        cors.setInitParameter(org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_ORIGINS_PARAM, "*");
        cors.setInitParameter(org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_HEADERS_PARAM, "Content-Type,Authorization,X-Requested-With,Content-Length,Accept,Origin");
        cors.setInitParameter(org.eclipse.jetty.servlets.CrossOriginFilter.ALLOWED_METHODS_PARAM, "GET,PUT,POST,DELETE,OPTIONS");
        cors.setInitParameter(org.eclipse.jetty.servlets.CrossOriginFilter.ALLOW_CREDENTIALS_PARAM, "true");
    }
}


