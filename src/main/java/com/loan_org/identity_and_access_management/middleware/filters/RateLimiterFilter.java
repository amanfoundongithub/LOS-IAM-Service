package com.loan_org.identity_and_access_management.middleware.filters;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.loan_org.identity_and_access_management.middleware.rateLimiter.RateLimiterService;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RateLimiterFilter extends OncePerRequestFilter {

    private static final String RATE_LIMIT_REMAINING_HEADER = "X-RateLimit-Remaining";
    private static final String RESPONSE_CONTENT_TYPE = "application/json";
    private static final String RESPONSE_CHARACTER_ENCODING = "UTF-8";

    private static final String RESPONSE_MESSAGE = """
            {
                "error": "TOO_MANY_REQUESTS",
                "status": 429,
                "timestamp": "%s",
                "message": "Too many requests. Please retry later."
            }
            """;

    private final String apiKeyHeader;
    private final String forwardedForHeader;
    private final String retryAfterHeader;
    private final RateLimiterService rateLimiterService;

    public RateLimiterFilter(
            RateLimiterService rateLimiterService,
            @Value("${filter.rateLimiter.apiKey.header}") String apiKeyHeader,
            @Value("${filter.rateLimiter.forwardedFor.header}") String forwardedForHeader,
            @Value("${filter.rateLimiter.retryAfter.header}") String retryAfterHeader) {

        this.rateLimiterService = rateLimiterService;
        this.apiKeyHeader = apiKeyHeader;
        this.forwardedForHeader = forwardedForHeader;
        this.retryAfterHeader = retryAfterHeader;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String identifier = resolveIdentifier(request);
        Bucket bucket = rateLimiterService.resolveBucket(identifier);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            response.setHeader(
                    RATE_LIMIT_REMAINING_HEADER,
                    String.valueOf(probe.getRemainingTokens())
            );
            filterChain.doFilter(request, response);
            return;
        }

        long retryAfterSeconds =
                Math.max(
                        1,
                        TimeUnit.NANOSECONDS.toSeconds(
                                probe.getNanosToWaitForRefill()
                        )
                );

        log.warn(
                "Rate limit exceeded. identifier={}, uri={}",
                identifier,
                request.getRequestURI()
        );

        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(RESPONSE_CONTENT_TYPE);
        response.setCharacterEncoding(RESPONSE_CHARACTER_ENCODING);

        response.setHeader(
                retryAfterHeader,
                String.valueOf(retryAfterSeconds)
        );

        response.getWriter().write(
                RESPONSE_MESSAGE.formatted(Instant.now())
        );
    }

    private String resolveIdentifier(HttpServletRequest request) {

        String identifier = request.getHeader(apiKeyHeader);
        if (identifier != null && !identifier.isBlank()) {
            return identifier;
        }

        identifier = request.getHeader(forwardedForHeader);
        if (identifier != null && !identifier.isBlank()) {
            return extractClientIp(identifier);
        }

        return request.getRemoteAddr();
    }

    private String extractClientIp(String forwardedForHeader) {
        return forwardedForHeader.split(",")[0].trim();
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator");
    }
}