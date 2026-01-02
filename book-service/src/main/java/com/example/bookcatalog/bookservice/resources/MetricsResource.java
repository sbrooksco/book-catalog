package com.example.bookcatalog.bookservice.resources;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.exporter.common.TextFormat;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Response;

import java.io.StringWriter;
import java.io.Writer;

@Path("/metrics")
public class MetricsResource {

    @GET
    @Produces(TextFormat.CONTENT_TYPE_004)
    public Response getMetrics() {
        try {
            Writer writer = new StringWriter();
            TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
            return Response.ok(writer.toString()).build();
        } catch (Exception e) {
            return Response.serverError().entity("Error generating metrics: " + e.getMessage()).build();
        }
    }
}
