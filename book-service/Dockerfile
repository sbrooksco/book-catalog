# Note that this configuration has you build the microservice from the monorepo root.
# If you want the microservice to build completely independently, then you can do that
# but you lose parent level version management.

# Stage 1: Build the JAR
FROM maven:3.9.2-eclipse-temurin-17 AS build

WORKDIR /app

# Copy the whole project (so Maven can resolve the parent POM)
COPY . .

# Build only the book-service module (disable tiered compilation for better performance)
ENV MAVEN_OPTS="-Xmx512m -XX:MaxMetaspaceSize=256m -XX:-TieredCompilation"
RUN mvn -pl book-service -am clean package -DskipTests

# Stage 2: Runtime Image (Just the JRE is needed)
FROM eclipse-temurin:17-jre-jammy

# Install curl for healthcheck
RUN apt-get update && \
    apt-get install -y curl && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

# Copy the JAR from the build stage.
COPY --from=build /app/book-service/target/book-service-*.jar ./book-service.jar
# The config from the build stage
COPY --from=build /app/book-service/src/main/resources/config.yaml ./config.yaml
COPY book-service/start.sh ./start.sh

RUN chmod +x start.sh

# Render will set PORT, we just expose it
EXPOSE ${PORT:-8080}

# Healthcheck on the admin port
#HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
#  CMD curl -f http://localhost:8081/healthcheck || exit 1

ENTRYPOINT ["./start.sh"]


