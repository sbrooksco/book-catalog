package com.example.bookcatalog.reviewservice.core;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.*;  // Changed from javax.persistence
import jakarta.validation.constraints.*;  // Changed from javax.validation

@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Maps JSON "bookId" to this field, and Hibernate to "book_id" column
    @JsonProperty("bookId")
    @NotNull(message = "bookId is required")  // Added so that some book id must be supplied.
    @Column(name = "book_id", nullable = false)
    private Integer bookId;

    @JsonProperty("reviewerName")
    @NotBlank(message = "reviewerName is required")
    @Column(name = "reviewer_name", nullable = false)
    private String reviewerName;

    @Min(value = 1, message = "rating must be at least 1")
    @Max(value = 5, message = "rating cannot be more than 5")
    @Column(nullable = false)
    private int rating;

    @NotBlank(message = "comment must not be blank")
    @Column
    private String comment;

    // Default constructor for Jackson/Hibernate
    public Review() {}

    public Review(Integer bookId, String reviewerName, int rating, String comment) {
        this.bookId = bookId;
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
    }

    // Getters and setters

    public Long getId() {
        return id;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public void setReviewerName(String reviewerName) {
        this.reviewerName = reviewerName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}



