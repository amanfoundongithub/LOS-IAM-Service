package com.loan_org.identity_and_access_management.middleware.filter;

import com.loan_org.identity_and_access_management.middleware.service.RateLimiterService;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
public class RateLimiterFilter extends OncePerRequestFilter {

    @Value("${filter.rateLimiter.apiKey.header}")
    private String apiKeyHeader;

    @Value("${filter.rateLimiter.forwardedFor.header}")
    private String forwardedForHeader;

    @Value("${filter.rateLimiter.retryAfter.header}")
    private String retryAfterHeader;

    @Value("${filter.rateLimiter.retryAfter.seconds}")
    private int retryAfterSeconds;

    private static final String RESPONSE_CHARACTER_ENCODING = "UTF-8";
    private static final String RESPONSE_CONTENT_TYPE = "application/json";

    private static final String RESPONSE_MESSAGE = """
            {
                "error" : "TOO_MANY_REQUESTS",
                "status": 429,
                "timestamp": "%s",
                "message": "You have sent too many requests. Please wait until the cache gets cleared."
            }
            """;

    private final RateLimiterService rateLimiterService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        // Resolve API key for rate limit validation
        String apiKey = request.getHeader(apiKeyHeader);
        if(apiKey == null || apiKey.trim().isEmpty()) {

            apiKey = request.getHeader(forwardedForHeader);
            if (apiKey == null || apiKey.isBlank()) {
                apiKey = request.getRemoteAddr();
            } else {
                apiKey = getHostName(apiKey);
            }

        }
        Bucket bucket = rateLimiterService.resolveBucket(apiKey);

        if(bucket.tryConsume(1)) {

            response.addHeader("X-RateLimit-Remaining", String.valueOf(bucket.getAvailableTokens()));
            filterChain.doFilter(request, response);

        } else {

            // Add basic 429 response for too many requests
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(RESPONSE_CONTENT_TYPE);
            response.setCharacterEncoding(RESPONSE_CHARACTER_ENCODING);
            response.getWriter().write(
                    String.format(
                            RESPONSE_MESSAGE,
                            Instant.now().toString()
                    )
            );


            // Add custom header for user to retry after a minute or so
            response.addHeader(retryAfterHeader, String.valueOf(retryAfterSeconds));

        }
    }

    private String getHostName(String forwardedHeaderValue) {
        return forwardedHeaderValue.split(",")[0].trim();
    }
}
