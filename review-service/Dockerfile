# Note that this configuration has you build the microservice from the monorepo root.
# If you want the microservice to build completely independently, then you can do that
# but you lose parent level version management.

# Stage 1: Build the JAR
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the whole project (so Maven can resolve the parent POM)
COPY . .

# Build only the review-service module (disable tiered compilation for better performance)
ENV MAVEN_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m -XX:-TieredCompilation"
RUN mvn -pl review-service -am clean package -DskipTests

# Stage 2: Runtime Image
FROM eclipse-temurin:17-jre-jammy

# Install curl for healthcheck
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/* \

WORKDIR /app

# Copy the jar and the config file from the build stage.
COPY --from=build /app/review-service/target/review-service-*.jar ./review-service.jar
COPY --from=build /app/review-service/src/main/resources/config.yaml ./config.yaml
COPY review-service/start.sh ./start.sh

RUN chmod +x start.sh

#EXPOSE 8082 8083
# Render will set PORT
EXPOSE ${PORT:-8082}

# Healthcheck on the admin port
#HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#  CMD curl -f http://localhost:8081/healthcheck || exit 1

#ENTRYPOINT ["java", "-jar", "review-service.jar", "server", "config.yaml"]
ENTRYPOINT ["./start.sh"]


