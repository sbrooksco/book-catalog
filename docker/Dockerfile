FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Copy the JAR from the build context (which will be project root)
COPY target/book-catalog-1.0-SNAPSHOT.jar app.jar

# Copy the config file
COPY src/main/resources/config.yaml config.yaml

# Expose Dropwizard ports
EXPOSE 8080 8081

# Default command to run the app
CMD ["java", "-jar", "app.jar", "server", "config.yaml"]
