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
9. Get a book by ID: (Not implemented)
   10. curl http://localhost:8080/books/1
11. Update a book
    12. curl -X PUT -H "Content-Type: application/json" \
        -d '{"title":"The Hobbit: Updated","author":"J.R.R. Tolkien"}' \
        http://localhost:8080/books/1
13. Delete a book by ID:
    14. curl -X DELETE http://localhost:8080/books/1
15. Stop the containers
    16. docker compose -f docker/docker-compose.yaml down

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
```
book-catalog/                  <-- project root (Maven build context)
├── pom.xml                    <-- Maven project file
├── target/                    <-- Maven output directory
│   └── book-catalog-1.0-SNAPSHOT.jar
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── org/example/
│   │   │       ├── BookCatalogApplication.java
│   │   │       ├── BookCatalogConfiguration.java
│   │   │       ├── core/Book.java
│   │   │       ├── db/BookDAO.java
│   │   │       └── resources/BookResource.java
│   │   └── resources/
│   │       └── config.yaml
│   └── test/                  <-- optional test classes
├── docker/                    <-- Docker-related files
│   ├── Dockerfile             <-- builds the app image
│   └── docker-compose.yaml    <-- runs app + db together
└── README.md
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


====
1. HibernateBundle (See BookCatalogApplication)
   2. Integrates Hibernate with Dropwizard
   3. Scans your Book entity for ORM mappings
2. BookDAO
   3. Createdd within the session factory from the Hibernate bundle
   4. Handles database CRUD operations
3. BookResource
   4. Your REST endpoint (/books)
   5. Registered with Jersey so it becomes available via HTTP
4. BookCatalogConfiguration
   5. Has a DataSourceFactory database field
   6. hibernate uses this for database config.
====
