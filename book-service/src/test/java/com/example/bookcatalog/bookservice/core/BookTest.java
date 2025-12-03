package com.example.bookcatalog.bookservice.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class BookTest {

    private Book testBook;

    @BeforeEach
    void setup() {
        testBook = new Book("Test Title", "Test Author");
        testBook.setId(1L);
    }

    @Test
    void testGettersAndSetters() {
        assertThat(testBook.getTitle()).isEqualTo("Test Title");
        assertThat(testBook.getAuthor()).isEqualTo("Test Author");
        assertThat(testBook.getId()).isEqualTo(1L);
    }

    @Test
    void testEquals() {
        Book testAnotherBook = new Book("Test Title", "Test Author");
        testAnotherBook.setId(1L);
        assertThat(testBook.equals(testAnotherBook)).isTrue();
    }

    @Test
    void testEqualsNotEqualTitle() {
        Book testAnotherBook = new Book("Test Another Title", "Test Author");
        testAnotherBook.setId(1L);
        assertThat(testBook.equals(testAnotherBook)).isFalse();
    }

    @Test
    void testEqualsNotEqualAuthor() {
        Book testAnotherBook = new Book("Test Title", "Test Another Author");
        testAnotherBook.setId(1L);
        assertThat(testBook.equals(testAnotherBook)).isFalse();
    }

    @Test
    void testEqualsNotEqualIsbn() {
        Book testAnotherBook = new Book("Test Title", "Test Author");
        testAnotherBook.setIsbn("1234567890");
        testAnotherBook.setId(1L);
        assertThat(testBook.equals(testAnotherBook)).isFalse();
    }

    @Test
    void testEqualsNotEqualBookId() {
        Book testAnotherBook = new Book("Test Title", "Test Author");;
        testAnotherBook.setId(2L);
        assertThat(testBook.equals(testAnotherBook)).isFalse();
    }

    @Test
    void testEqualsDuplicate() {
        Book testAnotherBook = testBook;
        assertThat(testBook.equals(testAnotherBook)).isTrue();
    }

    @Test
    void testNotEqualsClass() {
        Object notABook = new Object();
        assertThat(testBook.equals(notABook)).isFalse();
    }

    @Test
    void testHashCode() {
        Book testAnotherBook = new Book("Test Title", "Test Author");
        testAnotherBook.setId(1L);
        assertThat(testBook.hashCode() == testAnotherBook.hashCode()).isTrue();
    }
}
