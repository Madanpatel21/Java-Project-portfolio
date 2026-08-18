package com.java700.stewardship.common.web;

import com.java700.stewardship.common.api.Problems;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Fixed-window per-client rate limiting for the authentication endpoint. */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final int authPerMinute;

    public RateLimitFilter(@Value("${app.rate-limit.auth-per-minute:10}") int authPerMinute) {
        this.authPerMinute = authPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getRequestURI().startsWith("/api/v1/auth/")) {
            long now = System.currentTimeMillis();
            long bucket = now / 60_000;
            Window w = windows.compute(request.getRemoteAddr(), (k, cur) ->
                    cur == null || cur.bucket != bucket ? new Window(bucket, 0) : cur);
            if (++w.count > authPerMinute) {
                throw new Problems.RateLimited("Too many requests; retry after one minute");
            }
        }
        chain.doFilter(request, response);
    }

    private static final class Window {
        private final long bucket;
        private int count;

        private Window(long bucket, int count) {
            this.bucket = bucket;
            this.count = count;
        }
    }
}
