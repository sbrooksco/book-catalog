package com.example.bookcatalog.reviewservice.resources;

import io.dropwizard.hibernate.UnitOfWork;
import com.example.bookcatalog.reviewservice.core.Review;
import com.example.bookcatalog.reviewservice.db.ReviewDAO;

import jakarta.validation.Valid;
import jakarta.validation.ConstraintViolationException;
import jakarta.ws.rs.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Path("/reviews")
@Produces(MediaType.APPLICATION_JSON)
public class ReviewResource {

    private final ReviewDAO reviewDAO;
    private final Client client;
    private final String bookServiceUrl;

    public ReviewResource(ReviewDAO reviewDAO, Client client, String bookServiceUrl) {
        this.reviewDAO = reviewDAO;
        this.client = client;
        this.bookServiceUrl = bookServiceUrl;
    }

    // GET all reviews
    @GET
    @UnitOfWork
    public List<Review> getAllReviews() {
        return reviewDAO.findAll();
    }

    // GET a single review by ID
    @GET
    @Path("/{id}")
    @UnitOfWork
    public Response getReviewById(@PathParam("id") Long id) {
        Optional<Review> review = reviewDAO.findById(id);
        if (review.isPresent()) {
            return Response.ok(review.get()).build();
        } else {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Review with ID " + id + " not found")
                    .build();
        }
    }

    /**
     * Gets all reviews for a given book.
     *
     * @param bookId The ID of the book to get reviews for.
     * @return A list of all reviews for the given book.
     */
    @GET
    @Path("/book/{bookId}")
    @UnitOfWork
    public List<Review> getReviewsByBook(@PathParam("bookId") Integer bookId) {
        List<Review> allReviews = reviewDAO.findAll();
        System.out.println("DEBUG: Looking for reviews with bookId: " + bookId);
        System.out.println("DEBUG: Total reviews in database: " + allReviews.size());

        List<Review> filtered = allReviews.stream()
                .filter(review -> {
                    Integer reviewBookId = review.getBookId();
                    System.out.println("DEBUG: Review " + review.getId() + " has bookId: " + reviewBookId);
                    return reviewBookId != null && reviewBookId.equals(bookId);
                })
                .collect(Collectors.toList());

        System.out.println("DEBUG: Found " + filtered.size() + " reviews for book " + bookId);
        return filtered;
    }

    // GET books from book service
    @GET
    @Path("/books")
    public Response getBooksFromBookService() {
        WebTarget target = client.target(bookServiceUrl).path("/books");
        String booksJson = target.request(MediaType.APPLICATION_JSON).get(String.class);
        return Response.ok(booksJson).build();
    }

    /**
     * POST a new review
     *
     * Note that we handle the ContraintValidationException (due to the annotations on the
     * field in the entity Review.java (ex: @NotNull(message = "bookId is required")
     * so that we can return a 400 bad request.
     */
    @POST
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    public Response addReview(@Valid Review review) {
        // Fields are validated with annotations in the entity (Review.java).
        try {
            Review created = reviewDAO.create(review);
            return Response.status(Response.Status.CREATED)
                    .entity(created)
                    .build();
        } catch (ConstraintViolationException e) {
            // Extract all violations and return 400
            StringBuilder sb = new StringBuilder();
            e.getConstraintViolations().forEach(v -> sb.append(v.getPropertyPath())
                    .append(": ")
                    .append(v.getMessage())
                    .append("; "));
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(sb.toString())
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error creating review: " + e.getMessage())
                    .build();
        }
    }

    // PUT - update an existing review
    @PUT
    @Path("/{id}")
    @UnitOfWork
    @Consumes(MediaType.APPLICATION_JSON)
    public Response updateReview(@PathParam("id") Long id, @Valid Review updatedReview) {
        Optional<Review> existingOpt = reviewDAO.findById(id);

        if (existingOpt.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Review with ID " + id + " not found")
                    .build();
        }

        Review existing = existingOpt.get();

        // Update only fields provided in the payload
        if (updatedReview.getBookId() != null) {
            existing.setBookId(updatedReview.getBookId());
        }
        if (updatedReview.getReviewerName() != null && !updatedReview.getReviewerName().trim().isEmpty()) {
            existing.setReviewerName(updatedReview.getReviewerName());
        }
        if (updatedReview.getRating() > 0 && updatedReview.getRating() <= 5) {
            existing.setRating(updatedReview.getRating());
        }
        if (updatedReview.getComment() != null) {
            existing.setComment(updatedReview.getComment());
        }

        try {
            reviewDAO.update(existing);
            return Response.ok(existing).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error updating review: " + e.getMessage())
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @UnitOfWork
    public Response deleteReview(@PathParam("id") Long id) {
        Optional<Review> review = reviewDAO.findById(id);

        if (review.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("Review with ID " + id + " not found")
                    .build();
        }

        try {
            reviewDAO.delete(review.get());
            return Response.ok()
                    .entity("Review with ID " + id + " deleted successfully")
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error deleting review: " + e.getMessage())
                    .build();
        }
    }
}




