package org.example.resources;

import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.core.Response;
import org.example.core.Book;
import org.example.db.BookDAO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
class BookResourceTest {

    private static final BookDAO dao = mock(BookDAO.class);

    // Do NOT use 'static final'; ResourceExtension must be non-final for JUnit 5 support
    static final ResourceExtension RESOURCES = ResourceExtension.builder()
            .addResource(new BookResource(dao))
            .build();

    @Test
    void testGetBookById() {
        Book book = new Book("The Hobbit", "J.R.R. Tolkien");
        book.setId(1L);

        when(dao.findById(1L)).thenReturn(Optional.of(book));

        Response response = RESOURCES.target("/books/1").request().get();

        assertThat(response.getStatus()).isEqualTo(200);
        Book returnedBook = response.readEntity(Book.class);
        assertThat(returnedBook.getTitle()).isEqualTo("The Hobbit");
        assertThat(returnedBook.getAuthor()).isEqualTo("J.R.R. Tolkien");

        verify(dao).findById(1L);
    }

    @Test
    void testGetBookById_NotFound() {
        when(dao.findById(999L)).thenReturn(Optional.empty());

        Response response = RESOURCES.target("/books/999").request().get();

        assertThat(response.getStatus()).isEqualTo(Response.Status.NOT_FOUND.getStatusCode());
        verify(dao).findById(999L);
    }
}



