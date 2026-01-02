package com.example.bookcatalog.reviewservice.auth;

import com.auth0.jwk.Jwk;
import com.auth0.jwk.JwkProvider;
import com.auth0.jwk.JwkProviderBuilder;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;

public class JwtAuthFilter implements Filter {
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthFilter.class);
    private final JwkProvider jwkProvider;
    private final String clerkDomain;

    public JwtAuthFilter(String clerkDomain) {
        this.clerkDomain = clerkDomain;
        this.jwkProvider = new JwkProviderBuilder(clerkDomain).build();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Allow OPTIONS requests (CORS preflight)
        if ("OPTIONS".equals(httpRequest.getMethod())) {
            chain.doFilter(request, response);
            return;
        }

        // Allow health checks without auth
        String path = httpRequest.getRequestURI();
        if (path.equals("/metrics") ||
                path.contains("/healthcheck") ||
                path.contains("/admin")) {
            chain.doFilter(request, response);
            return;
        }

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

            // Token is valid, proceed with request
            chain.doFilter(request, response);

        } catch (Exception e) {
            LOGGER.warn("JWT validation failed: {}", e.getMessage());
            httpResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token");
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void destroy() {}
}
