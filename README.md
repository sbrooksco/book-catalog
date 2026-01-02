# BookCatalog

This project exemplifies how to create a dropwizard application to deploy a basic REST API 
that runs against a postgres database.

The postgres database and the bookcatalog application are containerized and deployable in
a kubernetes cluster.  The examples given here show how to deploy the containers or to the 
kubernetes cluster using colima.


# How to run the BookCatalog application
## Deploy just the docker containers:
```
colima start --vm-type=vz --kubernetes  (--kubernetes if you want k3 started)
docker compose -f docker/docker-compose.yaml up --build -d
```
-d is for detached mode, it runs in the background.
## If you need to force recreate the containers
```
docker compose -f docker/docker-compose.yaml up --build -d --force-recreate
```
Commands to check the logs:
```
    docker logs book-catalog
    docker logs bookdb
    docker logs review-service
    docker logs reviewdb
```
Take down the deployment
```
docker compose -f docker/docker-compose.yaml down
```

# Sanity Testing
## Test book-service
curl http://localhost:8080/books \
curl http://localhost:8081/healthcheck \
## Test review-service
curl http://localhost:8082/reviews \
curl http://localhost:8083/healthcheck \
## Test inter-service communication.
curl http://localhost:8082/reviews/books

# Deploy in kubernetes
```
# Start Colima with Kubernetes enabled
colima start --kubernetes

# Apply manifests

kubectl apply -f k8s/

# Alternatively you can deploy the manifiests one by one:
# Apply config and secrets first
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

# Deploy Postgres
kubectl apply -f k8s/postgres.yaml
kubectl rollout status deployment/postgres

# Deploy Book Catalog app
kubectl apply -f k8s/bookcatalog.yaml
kubectl rollout status deployment/book-catalog

# Verify everything
kubectl get pods
kubectl get svc

# Remember to forward the ports:
kubectl port-forward svc/book-catalog 8080:8080

Note, if it was in a different namespaces you wuld run it like this:
kubectl port-forward svc/book-catalog 8080:8080 -n <namespace>>


### Test your API
curl http://localhost:8080/books


# Clean up
kubectl delete -f k8s/

colima stop
```

# Example Operations on the endpoint

## Test the endpoint
    curl http://localhost:8080/books

## Add a book:
    curl -X POST -H "Content-Type: application/json" \
      -d '{"title":"The Hobbit","author":"J.R.R. Tolkien"}' \
      http://localhost:8080/books
## List the books
    curl http://localhost:8080/books
## Get a book by ID:
    curl http://localhost:8080/books/1
## Update a book
    curl -X PUT -H "Content-Type: application/json" \
        -d '{"title":"The Hobbit: Updated","author":"J.R.R. Tolkien"}' \
        http://localhost:8080/books/1
## Delete a book by ID:
    curl -X DELETE http://localhost:8080/books/1


# Health Check

To see the application's health enter url `http://localhost:8081/healthcheck`

# Unit Test Report

mvn clean test

project root/target/site/jacoco/index.html

# Non docker execution:
1. Start application with `java -jar target/book-catalog-1.0-SNAPSHOT.jar server config.yaml`
1. To check that your application is running enter url `http://localhost:8080`


# TODO
* Helm charts to simplify deployment
* Add a liveness probe
* Add a persistent volume for postgres
* Add a front end 
* Add a service LoadBalancer
* Expose the application via ingress instead of a LoadBalancer
* Add a Grafana dashboard for monitoring

-----------------
# Project Structure
```
book-catalog/
├── .git/
├── .github/
│   └── workflows/
│       └── java-ci.yaml                    # GitHub Actions CI pipeline
├── .gitignore
├── .idea/                                   # IntelliJ IDEA project files
├── README.md
├── pom.xml                                  # Parent Maven POM
├── render.yaml                              # Render.com deployment config
│
├── book-service/                            # Book microservice
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/bookcatalog/bookservice/
│       │   │       ├── BookServiceApplication.java
│       │   │       ├── resources/          # API endpoints
│       │   │       ├── dao/                # Database access
│       │   │       ├── models/             # Data models
│       │   │       └── ...
│       │   └── resources/
│       │       └── config.yml              # Dropwizard configuration
│       └── test/
│
├── review-service/                          # Review microservice
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
│       ├── main/
│       │   ├── java/
│       │   │   └── com/bookcatalog/reviewservice/
│       │   │       ├── ReviewServiceApplication.java
│       │   │       ├── resources/
│       │   │       ├── dao/
│       │   │       ├── models/
│       │   │       └── ...
│       │   └── resources/
│       │       └── config.yml
│       └── test/
│
├── docker/                                  # Docker & monitoring configuration
│   ├── docker-compose.yaml                 # Multi-service orchestration
│   ├── prometheus.yaml                     # Prometheus scrape config
│   ├── init.sql                            # Database initialization
│   ├── book-service-docker-compose.yaml    # (legacy/dev file?)
│   └── grafana/
│       ├── dashboards/
│       │   ├── book-service.json          # Book service dashboard
│       │   └── review-service.json        # Review service dashboard
│       └── provisioning/
│           ├── datasources/
│           │   └── prometheus.yaml        # Auto-configure Prometheus datasource
│           └── dashboards/
│               └── dashboard.yaml         # Dashboard provisioning config
│
├── k8s/                                     # Kubernetes manifests (future deployment)
│   └── ...
│
└── migrations/                              # Database migrations
    └── ...

External (separate repository):
└── book-catalog-frontend/                   # React frontend
    ├── Deployed on Vercel
    └── Uses Clerk for authentication
```
-----------------
```
┌─────────────────────────────────────────────────────────────┐
│                         Docker Compose                      │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌──────────────┐    ┌──────────────┐    ┌──────────────┐   │
│  │              │    │              │    │              │   │
│  │ book-service │◄───┤ PostgreSQL   │───►│review-service│   │
│  │   :8080/81   │    │   :5432      │    │  :8082/83    │   │
│  │              │    │              │    │              │   │
│  └──────┬───────┘    └──────────────┘    └──────┬───────┘   │
│         │                                         │         │
│         │ /metrics                    /metrics    │         │
│         │                                         │         │
│         └─────────────┬───────────────────────────┘         │
│                       │                                     │
│                       ▼                                     │
│                ┌──────────────┐                             │
│                │              │                             │
│                │  Prometheus  │                             │
│                │    :9090     │                             │
│                │              │                             │
│                └──────┬───────┘                             │
│                       │                                     │
│                       ▼                                     │
│                ┌──────────────┐                             │
│                │              │                             │
│                │   Grafana    │                             │
│                │    :3000     │                             │
│                │              │                             │
│                └──────────────┘                             │
│                                                             │
└─────────────────────────────────────────────────────────────┘

                         ▲
                         │ API calls
                         │
              ┌──────────────────┐
              │                  │
              │  React Frontend  │
              │  (Vercel/Clerk)  │
              │                  │
              └──────────────────┘
              
Key Technologies:

Backend: Java + Dropwizard (microservices framework)
Database: PostgreSQL 16
Monitoring: Prometheus + Grafana
Frontend: React (separate repo, deployed on Vercel)
Auth: Clerk
CI/CD: GitHub Actions
Container Orchestration: Docker Compose (local), Kubernetes (planned)
Deployment: Render.com (configured)
```


# Project Setup

Step 1. Create the framework
```
mvn archetype:generate \
  -DarchetypeGroupId=io.dropwizard.archetypes \
  -DarchetypeArtifactId=java-simple \
  -DarchetypeVersion=4.0.7 \
  -DgroupId=org.example \
  -DartifactId=book-catalog \
  -Dname=BookCatalog
```

2. Create <project root>/src/main/resources/config.yaml

3. Update BookCatalogConfiguration.java to create/set/get DataSourceFactory

4. Add dropwizard-db to pom.xml dependencies as a dependency

5. Build and run
```
mvn clean package
java -jar target/book-catalog-1.0-SNAPSHOT.jar server src/main/resources/config.yaml
```

STEP 2. Add classes for the entity, resource and data access object.

Create 
	Book.java
	BookDAO.java
	BookResource.java
Update
	BookCatalogApplication.java
		Instantiate HibernateBundle

		Update initialize() to add hibernate to the bundle
		Update run() to instantiate a BookDAO as dao 
				and register a BookResource(dao)

STEP 3. Set up the kubernetes configuration

Create K8S manifests for
    The PostgreSQL database (Deployment, Service, PVC)
    The Book Catalog API (Deployment, Service)
Configure environment variables and networking (so the app can talk to the DB service)
Optionally add a ConfigMap for your YAML config instead of baking it into the container.
Test deployment
