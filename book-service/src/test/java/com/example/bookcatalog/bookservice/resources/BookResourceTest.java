package com.example.bookcatalog.bookservice.resources;

import com.example.bookcatalog.bookservice.core.Book;
import com.example.bookcatalog.bookservice.db.BookDAO;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
class BookResourceTest {

    private static final BookDAO dao = mock(BookDAO.class);

    private static final ResourceExtension resources = ResourceExtension.builder()
            .addResource(new BookResource(dao))
            .build();

    private Book testBook;

    @BeforeEach
    void setup() {
        testBook = new Book("Test Title", "Test Author");
        testBook.setId(1L);
    }

    @AfterEach
    void tearDown() {
        reset(dao);
    }

    @Test
    void testGetBooks() {
        // Arrange
        List<Book> books = Arrays.asList(testBook);
        when(dao.findAll()).thenReturn(books);

        // Act
        List<Book> response = resources.target("/books")
                .request()
                .get(List.class);

        // Assert
        assertThat(response).isNotNull();
        verify(dao).findAll();
    }

    @Test
    void testGetBookById() {
        // Arrange
        when(dao.findById(1L)).thenReturn(Optional.of(testBook));

        // Act
        Response response = resources.target("/books/1")
                .request()
                .get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        verify(dao).findById(1L);
    }

    @Test
    void testGetBookByIdNotFound() {
        // Arrange
        when(dao.findById(999L)).thenReturn(Optional.empty());

        // Act
        Response response = resources.target("/books/999")
                .request()
                .get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(404);
        verify(dao).findById(999L);
    }

    @Test
    void testCreateBook() {
        // Arrange
        Book newBook = new Book("New Book", "New Author");
        when(dao.create(any(Book.class))).thenReturn(newBook);

        // Act
        Response response = resources.target("/books")
                .request()
                .post(Entity.entity(newBook, MediaType.APPLICATION_JSON));

        // Assert
        assertThat(response.getStatus()).isEqualTo(201);
        ArgumentCaptor<Book> captor = ArgumentCaptor.forClass(Book.class);
        verify(dao).create(captor.capture());
        assertThat(captor.getValue().getTitle()).isEqualTo("New Book");
    }

    @Test
    void testUpdateBook() {
        // Arrange
        Book updatedBook = new Book("Updated Title", "Updated Author");
        when(dao.findById(1L)).thenReturn(Optional.of(testBook));
        when(dao.update(any(Book.class))).thenReturn(testBook);

        // Act
        Response response = resources.target("/books/1")
                .request()
                .put(Entity.entity(updatedBook, MediaType.APPLICATION_JSON));

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        verify(dao).update(any(Book.class));
    }

    @Test
    void testDeleteBook() {
        // Arrange
        when(dao.findById(1L)).thenReturn(Optional.of(testBook));
        doNothing().when(dao).delete(any(Book.class));

        // Act
        Response response = resources.target("/books/1")
                .request()
                .delete();

        // Assert
        assertThat(response.getStatus()).isEqualTo(204);
        verify(dao).delete(testBook);
    }
}
