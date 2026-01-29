package org.example.djajbladibackend.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Wraps the response so X-Cache-Status (HIT | MISS) is set before the first byte is written.
 * Runs first (HIGHEST_PRECEDENCE) so the response is wrapped before any other filter writes.
 */
public class CacheHitTrackingFilter extends OncePerRequestFilter implements Ordered {

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        CacheStatusResponseWrapper wrappedResponse = new CacheStatusResponseWrapper(response);
        try {
            filterChain.doFilter(request, wrappedResponse);
        } finally {
            wrappedResponse.flushBuffer();
            CacheHitTrackingCache.clearStatus();
        }
    }
}
