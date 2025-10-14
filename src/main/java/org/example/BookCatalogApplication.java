package org.example;

import io.dropwizard.core.Application;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.hibernate.HibernateBundle;
import io.dropwizard.db.DataSourceFactory;
import org.example.core.Book;
import org.example.db.BookDAO;
import org.example.resources.BookResource;

public class BookCatalogApplication extends Application<BookCatalogConfiguration> {

    private final HibernateBundle<BookCatalogConfiguration> hibernate = new HibernateBundle<>(Book.class) {
        @Override
        public DataSourceFactory getDataSourceFactory(BookCatalogConfiguration configuration) {
            return configuration.getDataSourceFactory();
        }
    };

    @Override
    public void initialize(io.dropwizard.core.setup.Bootstrap<BookCatalogConfiguration> bootstrap) {
        bootstrap.addBundle(hibernate);
    }

    @Override
    public void run(BookCatalogConfiguration configuration, Environment environment) {
        final BookDAO dao = new BookDAO(hibernate.getSessionFactory());
        environment.jersey().register(new BookResource(dao));
    }

    @Override
    public String getName() {
        return "book-catalog";
    }

    public static void main(String[] args) throws Exception {
        new BookCatalogApplication().run(args);
    }
}

