package com.example.bookcatalog.reviewservice;

import io.dropwizard.core.Configuration;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.db.DataSourceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class ReviewServiceConfiguration extends Configuration {

    @Valid
    @NotNull
    private DataSourceFactory database = new DataSourceFactory();

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return database;
    }

    @JsonProperty("database")
    public void setDataSourceFactory(DataSourceFactory factory) {
        this.database = factory;
    }

    // --- Custom property: bookServiceUrl ---
    @NotEmpty
    private String bookServiceUrl;

    @JsonProperty
    public String getBookServiceUrl() {
        return bookServiceUrl;
    }

    @JsonProperty
    public void setBookServiceUrl(String bookServiceUrl) {
        this.bookServiceUrl = bookServiceUrl;
    }

    // --- Clerk authentication ---
    @NotNull
    private String clerkDomain;

    @JsonProperty("clerkDomain")
    public String getClerkDomain() {
        return clerkDomain;
    }

    @JsonProperty("clerkDomain")
    public void setClerkDomain(String clerkDomain) {
        this.clerkDomain = clerkDomain;
    }
}


