package com.example.bookcatalog.bookservice.db;

import com.example.bookcatalog.bookservice.core.Book;
import io.dropwizard.testing.junit5.DAOTestExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(DropwizardExtensionsSupport.class)
class BookDAOTest {

    private DAOTestExtension daoTestRule = DAOTestExtension.newBuilder()
            .addEntityClass(Book.class)
            .build();

    private BookDAO bookDAO;

    @BeforeEach
    void setUp() {
        bookDAO = new BookDAO(daoTestRule.getSessionFactory());
    }

    @Test
    void testCreateBook() {
        // Arrange
        Book book = new Book("Test Book", "Test Author");

        // Act
        Book created = daoTestRule.inTransaction(() -> bookDAO.create(book));

        // Assert
        assertThat(created.getId()).isGreaterThan(0);
        assertThat(created.getTitle()).isEqualTo("Test Book");
        assertThat(created.getAuthor()).isEqualTo("Test Author");
    }

    @Test
    void testFindById() {
        // Arrange
        Book book = new Book("Find Me", "Author");
        Book created = daoTestRule.inTransaction(() -> bookDAO.create(book));

        // Act
        Optional<Book> found = bookDAO.findById(created.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Find Me");
    }

    @Test
    void testFindAll() {
        // Arrange
        daoTestRule.inTransaction(() -> {
            bookDAO.create(new Book("Book 1", "Author 1"));
            bookDAO.create(new Book("Book 2", "Author 2"));
            return null;
        });

        // Act
        List<Book> books = bookDAO.findAll();

        // Assert
        assertThat(books).hasSizeGreaterThanOrEqualTo(2);
    }

    @Test
    void testUpdateBook() {
        // Arrange
        Book book = new Book("Original", "Author");
        Book created = daoTestRule.inTransaction(() -> bookDAO.create(book));

        // Act
        created.setTitle("Updated");
        daoTestRule.inTransaction(() -> bookDAO.update(created));

        Optional<Book> updated = bookDAO.findById(created.getId());

        // Assert
        assertThat(updated).isPresent();
        assertThat(updated.get().getTitle()).isEqualTo("Updated");
    }

    @Test
    void testDeleteBook() {
        // Arrange
        Book book = new Book("Delete Me", "Author");
        Book created = daoTestRule.inTransaction(() -> bookDAO.create(book));
        long id = created.getId();

        // Act
        daoTestRule.inTransaction(() -> {
            bookDAO.delete(created);
            return null;
        });

        Optional<Book> deleted = bookDAO.findById(id);

        // Assert
        assertThat(deleted).isEmpty();
    }
}
