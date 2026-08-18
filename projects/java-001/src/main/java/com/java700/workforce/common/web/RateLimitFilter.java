package com.java700.workforce.common.web;

import com.java700.workforce.common.api.Problems;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Fixed-window per-client rate limiting for the two most abuse-sensitive entry points:
 * authentication and access-event ingestion. Backed by an in-memory store in the dev
 * profile and by Redis in the local (production-like) profile via {@link RateLimitStore}.
 */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitStore store;
    private final int ingestPerMinute;
    private final int authPerMinute;

    public RateLimitFilter(RateLimitStore store,
                           @Value("${app.rate-limit.ingest-per-minute:120}") int ingestPerMinute,
                           @Value("${app.rate-limit.auth-per-minute:10}") int authPerMinute) {
        this.store = store;
        this.ingestPerMinute = ingestPerMinute;
        this.authPerMinute = authPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        int limit = path.startsWith("/api/v1/auth/") ? authPerMinute
                : path.startsWith("/api/v1/events/") ? ingestPerMinute : -1;
        if (limit > 0) {
            String key = request.getRemoteAddr() + "|" + (path.startsWith("/api/v1/auth/") ? "auth" : "ingest");
            if (!store.allow(key, limit)) {
                throw new Problems.RateLimited("Too many requests; retry after one minute");
            }
        }
        chain.doFilter(request, response);
    }
}
