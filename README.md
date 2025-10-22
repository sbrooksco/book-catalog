# BookCatalog

How to start the BookCatalog application
---

1. Run `mvn clean install` to build your application
2. Run docker compose to start the containers
   3. docker compose -f docker/docker-compose.yaml up --build -d
3. Tail logs to see Dropwizard connect to Postgres:
   4. docker compose -f docker/docker-compose.yaml logs -f app
5. Test the endpoint
   6. curl http://localhost:8080/books
7. Add a book:
   8. curl -X POST -H "Content-Type: application/json" \
      -d '{"title":"The Hobbit","author":"J.R.R. Tolkien"}' \
      http://localhost:8080/books
9. Get the books
   10. curl http://localhost:8080/books
9. Get a book by ID:
   10. curl http://localhost:8080/books/1
11. Update a book
    12. curl -X PUT -H "Content-Type: application/json" \
        -d '{"title":"The Hobbit: Updated","author":"J.R.R. Tolkien"}' \
        http://localhost:8080/books/1
13. Delete a book by ID:
    14. curl -X DELETE http://localhost:8080/books/1
15. Stop the containers
    16. docker compose -f docker/docker-compose.yaml down


Commands to check the logs:
```
    docker logs book-catalog
    docker logs bookdb
```
+++

Intentional error:

% curl http://localhost:8080/books/1 {"code":405,"message":"HTTP 405 Method Not Allowed"}%

So I checked and BookResource.java was missing
@GET
    @Path("/{id}")
    @UnitOfWork
    public Book getBook(@PathParam("id") int id) {   // <-- should handle GET /books/{id}
        return dao.findById(id);
    }


+++
Non docker execution:
1. Start application with `java -jar target/book-catalog-1.0-SNAPSHOT.jar server config.yaml`
1. To check that your application is running enter url `http://localhost:8080`

Health Check
---

To see your applications health enter url `http://localhost:8081/healthcheck`

-----------------
Project Structure:
```
book-catalog/
├── Dockerfile
├── README.md
├── pom.xml
├── target/
│   └── book-catalog-1.0-SNAPSHOT.jar
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/
│   │   │       └── example/
│   │   │           └── bookcatalog/
│   │   │               ├── BookCatalogApplication.java
│   │   │               ├── BookCatalogConfiguration.java
│   │   │               ├── core/
│   │   │               │   └── Book.java
│   │   │               ├── db/
│   │   │               │   └── BookDAO.java
│   │   │               ├── resources/
│   │   │               │   └── BookResource.java
│   │   │               └── health/
│   │   │                   └── DatabaseHealthCheck.java
│   │   ├── resources/
│   │   │   ├── banner.txt
│   │   │   ├── config.yml
│   │   │   └── db/
│   │   │       ├── V1__create_books_table.sql
│   │   │       └── V2__add_indexes.sql  ← (future migrations)
│   │   └── webapp/                      ← (optional if you serve static assets)
│   └── test/
│       └── java/
│           └── org/
│               └── example/
│                   └── bookcatalog/
│                       └── BookResourceTest.java
│
├── k8s/
│   ├── postgres.yaml                ← PostgreSQL Deployment + Service
│   ├── bookcatalog.yaml             ← Dropwizard App Deployment + Service
│   ├── bookcatalog-configmap.yaml
│   ├── bookcatalog-secret.yaml
│   └── namespace.yaml               ← (optional, for namespace isolation)
│
├── scripts/
│   ├── build.sh                     ← builds app + docker image
│   ├── run-local.sh                 ← starts containers locally with Docker Compose
│   └── load-images.sh               ← loads images into Colima’s Docker runtime
│
├── docker-compose.yml               ← optional for local testing
└── .gitignore
```
-----------------

Setup

1. 
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

STEP 2.

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

NOTE:  This is diverging from the hello-dropwizard app in that it is really using a database
so we need MIGRATIONS as a way to create tables and to update the schema.

STEP 3. (KUBERNETES)
Create K8S manifests for
    The PostgreSQL database (Deployment, Service, PVC)
    The Book Catalog API (Deployment, Service)
Configure environment variables and networking (so the app can talk to the DB service)
Optionally add a ConfigMap for your YAML config instead of baking it into the container.
Test deployment


Optional:
    Add a Service LoadBalancer
    Add Database Migrations

```

```
Do this on Thursday.  
Create the docker images as tar files locally to avoid pushing to /pulling from a registry
like Docker Hub or Git Hub etc.

# Start Colima with Kubernetes enabled
colima start --kubernetes

# Docker build the app image:

From the project root

docker build -t book-catalog:latest -f docker/Dockerfile .

    Explanation:

    -f docker/Dockerfile → tells Docker where the Dockerfile is.

    . → sets the build context to the project root, so src/ and target/ are visible.

*Note: This runs the docker image so you can log in and take a look at the contents:*

docker run -it --rm book-catalog:latest bash

*Note: You are using the base postgres:16 image so nothing to build.
      You would build if you make a custom image.*


# Apply manifests
### Apply config and secrets first
kubectl apply -f k8s/bookcatalog-configmap.yaml

kubectl apply -f k8s/bookcatalog-secret.yaml

# Deploy Postgres
kubectl apply -f k8s/postgres.yaml

kubectl rollout status deployment/postgres


# Run database migration job  SKIP FOR NOW
kubectl apply -f k8s/migrate-job.yaml
kubectl logs -f job/bookcatalog-migrate

# Deploy Book Catalog app
kubectl apply -f k8s/bookcatalog.yaml

kubectl rollout status deployment/book-catalog

# Test it
kubectl port-forward svc/book-catalog 8080:8080


curl http://localhost:8080/books


# Clean up

k get deployment
k delete deployment book-catalog
k delete deployment postgres

?? What about the config map and the secret?

colima stop

-------------------------------------------------------------
**Export them as tarballs:**

docker save -o book-catalog.tar book-catalog:latest
docker save -o hellodb.tar hellodb:custom

**Load them into colima:**

colima nerdctl load -i book-catalog.tar
colima nerdctl load -i hellodb.tar




# Start Colima with Kubernetes enabled
colima start --kubernetes

# Load images into Colima
colima nerdctl load -i book-catalog.tar
colima nerdctl load -i hellodb.tar

# Apply manifests
### Apply config and secrets first
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml

### Deploy Postgres
kubectl apply -f k8s/postgres.yaml
kubectl rollout status deployment/postgres

### Run database migration job
kubectl apply -f k8s/migrate-job.yaml
kubectl logs -f job/bookcatalog-migrate

### Deploy Book Catalog app
kubectl apply -f k8s/bookcatalog.yaml
kubectl rollout status deployment/book-catalog


### Verify everything
kubectl get pods
kubectl get svc

### Test your API
curl http://localhost:30080/books







====
1. HibernateBundle (See BookCatalogApplication)
   2. Integrates Hibernate with Dropwizard
   3. Scans your Book entity for ORM mappings
2. BookDAO
   3. Created within the session factory from the Hibernate bundle
   4. Handles database CRUD operations
3. BookResource
   4. Your REST endpoint (/books)
   5. Registered with Jersey so it becomes available via HTTP
4. BookCatalogConfiguration
   5. Has a DataSourceFactory database field
   6. hibernate uses this for database config.
====
