# Review Service

This sub project is intended to be a microservice that allows books to be entered and listed

# How to build the docker container:
From the project root:\
docker build -f book-service/docker/Dockerfile -t book-service:latest .

# How to run the book-service application standalone
## Start the Database:
docker run -d \
--name bookdb \
-e POSTGRES_USER=bookuser \
-e POSTGRES_PASSWORD=bookpass \
-e POSTGRES_DB=bookdb \
-p 5432:5432 \
postgres:16

## Now run book-service
docker run -p 8080:8080 -p 8081:8081 \
-e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/bookdb \
-e DATABASE_USER=bookuser \
-e DATABASE_PASSWORD=bookpass \
book-service:latest

# Check health
curl http://localhost:8081/healthcheck

# OR Use docker-compose (from the project root)
docker-compose -f docker/book-service-docker-compose.yaml up --build