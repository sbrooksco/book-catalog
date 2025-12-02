package com.example.bookcatalog.reviewservice.db;

import io.dropwizard.hibernate.AbstractDAO;
import com.example.bookcatalog.reviewservice.core.Review;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;

import java.util.List;
import java.util.Optional;

public class ReviewDAO extends AbstractDAO<Review> {

    public ReviewDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public List<Review> findAll() {
        Query<Review> query = currentSession().createQuery("FROM Review", Review.class);
        return list(query);
    }

    public Optional<Review> findById(Long id) {
        return Optional.ofNullable(get(id));
    }

    public Review create(Review review) {
        currentSession().persist(review);
        return review;
    }

    public void delete(Review review) {
        currentSession().delete(review);
    }

    public void update(Review review) {
        currentSession().saveOrUpdate(review);
    }
}




