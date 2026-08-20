package com.java700.achain.common.web;

import com.java700.achain.common.api.Problems;
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

/** Fixed-window per-client rate limiting for auth and public verification entry points. */
@Component
@Order(1)
public class RateLimitFilter extends OncePerRequestFilter {

    private final Map<String, Window> windows = new ConcurrentHashMap<>();
    private final int authPerMinute;
    private final int verifyPerMinute;

    public RateLimitFilter(@Value("${app.rate-limit.auth-per-minute:10}") int authPerMinute,
                           @Value("${app.rate-limit.verify-per-minute:60}") int verifyPerMinute) {
        this.authPerMinute = authPerMinute;
        this.verifyPerMinute = verifyPerMinute;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        int limit = path.startsWith("/api/v1/auth/") ? authPerMinute
                : path.startsWith("/api/v1/verify/") ? verifyPerMinute : -1;
        if (limit > 0) {
            long now = System.currentTimeMillis();
            long bucket = now / 60_000;
            String key = request.getRemoteAddr() + "|" + (path.startsWith("/api/v1/auth/") ? "auth" : "verify");
            Window w = windows.compute(key, (k, cur) ->
                    cur == null || cur.bucket != bucket ? new Window(bucket, 0) : cur);
            if (++w.count > limit) {
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
