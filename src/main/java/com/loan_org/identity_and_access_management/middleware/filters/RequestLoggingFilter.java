package com.loan_org.identity_and_access_management.middleware.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID = "correlationId";
    private static final String TRACE_ID = "traceId";

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {

        String uri = request.getRequestURI();

        return uri.startsWith("/actuator")
                || uri.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.nanoTime();

        String uri = buildUri(request);
        String clientIp = getClientIp(request);

        logIncoming(request, uri, clientIp);

        try {

            filterChain.doFilter(request, response);

        } catch (Exception ex) {

            log.error(
                    "Request failed method={} uri={} ip={} correlationId={} traceId={} message={}",
                    request.getMethod(),
                    uri,
                    clientIp,
                    MDC.get(CORRELATION_ID),
                    MDC.get(TRACE_ID),
                    ex.getMessage(),
                    ex
            );

            throw ex;

        } finally {

            long durationMs =
                    TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);

            logCompleted(
                    request,
                    response,
                    uri,
                    clientIp,
                    durationMs
            );
        }
    }

    private void logIncoming(
            HttpServletRequest request,
            String uri,
            String clientIp) {

        log.info(
                "Incoming request method={} uri={} ip={} correlationId={} traceId={}",
                request.getMethod(),
                uri,
                clientIp,
                MDC.get(CORRELATION_ID),
                MDC.get(TRACE_ID)
        );
    }

    private void logCompleted(
            HttpServletRequest request,
            HttpServletResponse response,
            String uri,
            String clientIp,
            long durationMs) {

        int status = response.getStatus();
        String user = getCurrentUser();

        if (status >= 500) {

            log.error(
                    "Completed request method={} uri={} status={} durationMs={} ip={} user={} correlationId={} traceId={}",
                    request.getMethod(),
                    uri,
                    status,
                    durationMs,
                    clientIp,
                    user,
                    MDC.get(CORRELATION_ID),
                    MDC.get(TRACE_ID)
            );

        } else if (status >= 400) {

            log.warn(
                    "Completed request method={} uri={} status={} durationMs={} ip={} user={} correlationId={} traceId={}",
                    request.getMethod(),
                    uri,
                    status,
                    durationMs,
                    clientIp,
                    user,
                    MDC.get(CORRELATION_ID),
                    MDC.get(TRACE_ID)
            );

        } else {

            log.info(
                    "Completed request method={} uri={} status={} durationMs={} ip={} user={} correlationId={} traceId={}",
                    request.getMethod(),
                    uri,
                    status,
                    durationMs,
                    clientIp,
                    user,
                    MDC.get(CORRELATION_ID),
                    MDC.get(TRACE_ID)
            );
        }
    }

    private String buildUri(HttpServletRequest request) {

        String uri = request.getRequestURI();

        if (request.getQueryString() != null) {
            uri += "?" + request.getQueryString();
        }

        return uri;
    }

    private String getClientIp(HttpServletRequest request) {

        return Optional.ofNullable(request.getHeader("X-Forwarded-For"))
                .map(ip -> ip.split(",")[0].trim())
                .orElse(request.getRemoteAddr());
    }

    private String getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return "anonymous";
        }

        return authentication.getName();
    }
}