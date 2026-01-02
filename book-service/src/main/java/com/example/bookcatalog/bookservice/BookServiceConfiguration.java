package com.example.bookcatalog.bookservice;

import io.dropwizard.core.Configuration;
import io.dropwizard.db.DataSourceFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public class BookServiceConfiguration extends Configuration {
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
