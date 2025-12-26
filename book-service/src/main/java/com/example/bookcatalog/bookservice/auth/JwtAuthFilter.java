package com.example.bookcatalog.bookservice.auth;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;

public class JwtAuthFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwkProvider jwkProvider;
    private final String clerkDomain;
    private final ObjectMapper objectMapper;

    public JwtAuthFilter(String clerkDomain) {
        this.clerkDomain = clerkDomain;
        this.jwkProvider = new JwkProviderBuilder(clerkDomain).build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();

        // Allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(method)) {
            chain.doFilter(request, response);
            return;
        }

        // Allow health checks without auth
        if (path.equals("/metrics") ||
                path.contains("/healthcheck") ||
                path.contains("/admin")) {
            chain.doFilter(request, response);
            return;
        }

        // Allow GET /books and GET /books/search without authentication (for anonymous browsing)
        if ("GET".equals(method) && (path.matches(".*/books/?$") || path.matches(".*/books/search.*"))) {
            chain.doFilter(request, response);
            return;
        }

        // Allow GET /books/{id} without authentication
        if ("GET".equals(method) && path.matches(".*/books/\\d+$")) {
            chain.doFilter(request, response);
            return;
        }

        // All other requests require authentication
        String authHeader = httpRequest.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or invalid Authorization header");
            return;
        }

        String token = authHeader.substring(7);

        try {
            // Decode the JWT
            DecodedJWT jwt = JWT.decode(token);

            // Get the public key from Clerk's JWKS endpoint
            Jwk jwk = jwkProvider.get(jwt.getKeyId());
            Algorithm algorithm = Algorithm.RSA256((RSAPublicKey) jwk.getPublicKey(), null);

            // Verify the token
            algorithm.verify(jwt);

            // Extract user role from JWT
            String role = extractRoleFromJWT(jwt);
            LOGGER.debug("User role: {}", role);

            // Check if DELETE or PUT requests require admin role
            if ("DELETE".equals(method) || "PUT".equals(method)) {
                if (!"admin".equals(role)) {
                    LOGGER.warn("Non-admin user attempted {} request", method);
                    httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required");
                    return;
                }
            }

            // Set user info in request attributes for downstream use (if needed)
            request.setAttribute("userId", jwt.getSubject());
            request.setAttribute("userRole", role);

            // Token is valid, proceed with request
            chain.doFilter(request, response);

        } catch (Exception e) {
            LOGGER.warn("JWT validation failed: {}", e.getMessage());
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }

    private String extractRoleFromJWT(DecodedJWT jwt) {
        try {
            // Clerk stores user metadata in the JWT
            // The structure is: claims -> public_metadata -> role
            String payload = new String(Base64.getUrlDecoder().decode(jwt.getPayload()));
            JsonNode payloadJson = objectMapper.readTree(payload);

            JsonNode publicMetadata = payloadJson.get("public_metadata");
            if (publicMetadata != null && publicMetadata.has("role")) {
                String role = publicMetadata.get("role").asText();
                LOGGER.debug("Extracted role from JWT: {}", role);
                return role;
            }

            LOGGER.debug("No role found in JWT, defaulting to 'user'");
            return "user"; // Default role
        } catch (Exception e) {
            LOGGER.error("Error extracting role from JWT: {}", e.getMessage());
            return "user";
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}