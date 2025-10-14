package org.example.resources;

import io.dropwizard.hibernate.UnitOfWork;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.example.core.Book;
import org.example.db.BookDAO;

import java.util.List;
import java.util.Optional;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    private final BookDAO dao;

    public BookResource(BookDAO dao) {
        this.dao = dao;
    }

    // GET /books - list all books
    @GET
    @UnitOfWork
    public List<Book> getBooks() {
        return dao.findAll();
    }

    // GET /books/{id} - get a single book by id
    @GET
    @Path("/{id}")
    @UnitOfWork
    public Response getBook(@PathParam("id") long id) {
        Optional<Book> book = dao.findById(id);
        return book.map(value -> Response.ok(value).build())
                .orElse(Response.status(Response.Status.NOT_FOUND).build());
    }

    // POST /books - create a new book
    @POST
    @UnitOfWork
    public Response createBook(Book book) {
        Book created = dao.create(book);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    // PUT /books/{id} - update an existing book
    @PUT
    @Path("/{id}")
    @UnitOfWork
    public Response updateBook(@PathParam("id") long id, Book book) {
        Optional<Book> existing = dao.findById(id);
        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        Book updated = existing.get();
        updated.setTitle(book.getTitle());
        updated.setAuthor(book.getAuthor());
        dao.update(updated);
        return Response.ok(updated).build();
    }

    // DELETE /books/{id} - delete a book
    @DELETE
    @Path("/{id}")
    @UnitOfWork
    public Response deleteBook(@PathParam("id") long id) {
        Optional<Book> existing = dao.findById(id);
        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        dao.delete(existing.get());
        return Response.noContent().build();
    }
}
