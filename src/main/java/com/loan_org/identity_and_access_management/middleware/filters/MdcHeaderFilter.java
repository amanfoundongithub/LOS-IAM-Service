package com.loan_org.identity_and_access_management.middleware.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcHeaderFilter extends OncePerRequestFilter {

    private static final String CORRELATION_PREFIX = "CORR-LOS-";

    private final String correlationHeader;
    private final String mdcCorrelationKey;
    private final String traceHeader;
    private final String mdcTraceKey;

    public MdcHeaderFilter(
            @Value("${filter.mdc.correlation.header}") String correlationHeader,
            @Value("${filter.mdc.correlation.key}") String mdcCorrelationKey,
            @Value("${filter.mdc.trace.header}") String traceHeader,
            @Value("${filter.mdc.trace.key}") String mdcTraceKey) {
        this.correlationHeader = correlationHeader;
        this.mdcCorrelationKey = mdcCorrelationKey;
        this.traceHeader = traceHeader;
        this.mdcTraceKey = mdcTraceKey;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request);
        String traceId = resolveTraceId(request);
        try {
            MDC.put(mdcCorrelationKey, correlationId);
            MDC.put(mdcTraceKey, traceId);
            response.setHeader(correlationHeader, correlationId);
            response.setHeader(traceHeader, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(correlationHeader);
        if (correlationId == null || correlationId.isBlank()) {
            return CORRELATION_PREFIX + UUID.randomUUID();
        }
        return correlationId;
    }

    private String resolveTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(traceHeader);
        if (traceId == null || traceId.isBlank()) {
            return UUID.randomUUID().toString().replace("-", "");
        }
        return traceId;
    }
}