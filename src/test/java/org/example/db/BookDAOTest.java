package org.example.db;

import io.dropwizard.testing.junit5.DAOTestExtension;
import org.example.core.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BookDAOTest {

    @RegisterExtension
    static final DAOTestExtension daoTestRule = DAOTestExtension.newBuilder()
            .addEntityClass(Book.class)
            .build();

    private BookDAO bookDAO;

    @BeforeEach
    void setUp() {
        bookDAO = new BookDAO(daoTestRule.getSessionFactory());
    }

    @Test
    void testPersistAndFindById() {
        Book book = daoTestRule.inTransaction(() -> {
            Book b = new Book("Dune", "Frank Herbert");
            bookDAO.create(b); // use create instead of update for new entities
            return b;
        });

        Optional<Book> fetched = daoTestRule.inTransaction(() -> bookDAO.findById(book.getId()));
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getTitle()).isEqualTo("Dune");
    }

    @Test
    void testFindAll() {
        daoTestRule.inTransaction(() -> {
            bookDAO.create(new Book("Dune", "Frank Herbert"));
            bookDAO.create(new Book("1984", "George Orwell"));
        });

        List<Book> books = daoTestRule.inTransaction(() -> bookDAO.findAll());
        assertThat(books).hasSize(2)
                .extracting(Book::getTitle)
                .containsExactlyInAnyOrder("Dune", "1984");
    }
}

