package com.example.bookcatalog.bookservice.db;

import io.dropwizard.hibernate.AbstractDAO;
import com.example.bookcatalog.bookservice.core.Book;
import org.hibernate.SessionFactory;

import java.util.List;
import java.util.Optional;

public class BookDAO extends AbstractDAO<Book> {

    public BookDAO(SessionFactory factory) {
        super(factory);
    }

    public Optional<Book> findById(long id) {
        return Optional.ofNullable(get(id));
    }

    public Book create(Book book) {
        return persist(book);
    }

    // Persist is protected in AbstractDAO so we need to wrap it here in
    // order for it to be used in BookResource.
    public Book update(Book book) {
        return persist(book); // persist can be used for updates
    }

    public void delete(Book book) {
        currentSession().delete(book);
    }

    public List<Book> findAll() {
        return list(namedTypedQuery("com.example.bookcatalog.bookservice.core.Book.findAll"));
    }
}
