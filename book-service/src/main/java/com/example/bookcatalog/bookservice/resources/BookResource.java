package com.example.bookcatalog.bookservice.resources;

import io.dropwizard.hibernate.UnitOfWork;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import com.example.bookcatalog.bookservice.core.Book;
import com.example.bookcatalog.bookservice.db.BookDAO;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/books")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class BookResource {

    private final BookDAO dao;

    public BookResource(BookDAO dao) {
        this.dao = dao;
    }

    /**
     * Searches for books by title, author, or year.
     * If a parameter is empty or null, it is ignored.
     * If a parameter is not empty or null, it is used to filter the results.
     * The results are case-insensitive.
     *
     * @param title the title of the book
     * @param author the author of the book
     * @param year the year of the book
     * @return a list of books that match the search criteria
     */
    @GET
    @Path("/search")
    @UnitOfWork
    public List<Book> searchBooks(@QueryParam("title") String title,
                                  @QueryParam("author") String author,
                                  @QueryParam("year") Integer year) {

        // NOTE, this is a simple in-memory search.  In a production environment, this would be a database search.
        List<Book> allBooks = dao.findAll();
        return allBooks.stream()
                .filter(book -> title == null || title.isEmpty() ||
                        book.getTitle().toLowerCase().contains(title.toLowerCase()))
                .filter(book -> author == null || author.isEmpty() ||
                        book.getAuthor().toLowerCase().contains(author.toLowerCase()))
                .filter(book -> year == null ||
                        (book.getPublishedYear() != null && book.getPublishedYear().equals(year)))
                .collect(Collectors.toList());
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
    public Response createBook(@Valid Book book) {
        Book created = dao.create(book);
        return Response.status(Response.Status.CREATED).entity(created).build();
    }

    // PUT /books/{id} - update an existing book
    @PUT
    @Path("/{id}")
    @UnitOfWork
    public Response updateBook(@PathParam("id") long id, @Valid Book book) {
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
