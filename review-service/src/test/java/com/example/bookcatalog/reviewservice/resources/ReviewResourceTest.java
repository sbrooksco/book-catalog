package com.example.bookcatalog.reviewservice.resources;

import com.example.bookcatalog.reviewservice.core.Review;
import com.example.bookcatalog.reviewservice.db.ReviewDAO;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(DropwizardExtensionsSupport.class)
class ReviewResourceTest {

    private static final ReviewDAO dao = mock(ReviewDAO.class);
    private static final Client client = mock(Client.class);
    private static final String bookServiceUrl = "http://localhost:8080";

    private static final ResourceExtension resources = ResourceExtension.builder()
            .addResource(new ReviewResource(dao, client, bookServiceUrl))
            .build();

    private Review testReview;

    @BeforeEach
    void setup() {
        testReview = new Review(1, "Test Reviewer", 5, "Great book!");
    }

    @AfterEach
    void tearDown() {
        reset(dao, client);
    }

    @Test
    void testGetAllReviews() {
        // Arrange
        when(dao.findAll()).thenReturn(Arrays.asList(testReview));

        // Act
        Response response = resources.target("/reviews")
                .request()
                .get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        verify(dao).findAll();
    }

    @Test
    void testGetReviewById() {
        // Arrange
        when(dao.findById(1L)).thenReturn(Optional.of(testReview));

        // Act
        Response response = resources.target("/reviews/1")
                .request()
                .get();

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        verify(dao).findById(1L);
    }

    @Test
    void testAddReview() {
        // Arrange
        Review newReview = new Review(1, "New Reviewer", 4, "Good book");
        when(dao.create(any(Review.class))).thenReturn(newReview);

        // Act
        Response response = resources.target("/reviews")
                .request()
                .post(Entity.entity(newReview, MediaType.APPLICATION_JSON));

        // Assert
        assertThat(response.getStatus()).isEqualTo(201);
        verify(dao).create(any(Review.class));
    }

    @Test
    void testDeleteReview() {
        // Arrange
        when(dao.findById(1L)).thenReturn(Optional.of(testReview));
        doNothing().when(dao).delete(any(Review.class));

        // Act
        Response response = resources.target("/reviews/1")
                .request()
                .delete();

        // Assert
        assertThat(response.getStatus()).isEqualTo(200);
        verify(dao).delete(testReview);
    }
}
