# Review Service

This sub project is intended to be a microservice that allows user reviews
to be recorded for a given book.


# How to run the review-service application standalone

#### From the root of the book-catalog project

java -jar review-service/target/review-service-1.0-SNAPSHOT-all.jar server review-service/src/main/resources/config.yaml
java -jar review-service/target/review-service-1.0-SNAPSHOT-all.jar server review-service/config.yaml

#### POST a review
curl -X POST http://localhost:8080/reviews \
-H "Content-Type: application/json" \
-d '{"bookId":1,"reviewerName":"Alice","rating":5,"comment":"Excellent!"}'

#### GET the reviews
curl http://localhost:8080/reviews

#### GET a single review by id
curl http://localhost:8080/reviews/3

#### UPDATE a single review by id
curl -X PUT http://localhost:8080/reviews/2 \
-H "Content-Type: application/json" \
-d '{"rating":4,"comment":"Pretty good, not perfect."}'

#### DELETE a single review by id
curl -X DELETE http://localhost:8080/reviews/3

### NOTE: Remember that postgres must be running on your laptop for this to work
(You also have to have the table set up)
```
brew services start postgresql@16
brew services stop postgresql@16
```

# How to run the review-service application using Docker

#### At the project root (book-catalog)
docker build -f review-service/docker/Dockerfile -t review-service:latest .

#### Verify that docker sees the image:
```
% docker images | grep review-service
review-service                        latest                 2b523041c754   2 minutes ago   628MB
```


#### At project root here is how you run the image  
# Remember that you need a postgres container running
# Simple run
docker run -d \
--name review-service \
-p 8080:8080 \
review-service:latest

# Check the container

docker ps 

CONTAINER ID   IMAGE                 COMMAND                  STATUS         PORTS                    NAMES
e7f3a3b1c2d4   review-service:latest "java -jar review-se…"   Up 10 seconds  0.0.0.0:8080->8080/tcp   review-service

# Check Logs 

docker logs -f review-service

# Test it
curl http://localhost:8080/reviews

# Shut it down

docker stop review-service
docker rm review-service

## Here you can run it using docker-compose from the project root

docker-compose -f docker/docker-compose.yaml up --build 