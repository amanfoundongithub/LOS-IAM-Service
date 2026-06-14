package com.loan_org.identity_and_access_management.middleware.filter;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Slf4j
public class MdcHeaderFilter extends OncePerRequestFilter {

    /**
     * Correlation configurations
     */
    @Value("${filter.mdc.correlation.header}")
    private String correlationHeader;

    @Value("${filter.mdc.correlation.key}")
    private String mdcCorrelationKey;

    /**
     * Trace configurations
     */
    @Value("${filter.mdc.trace.header}")
    private String traceHeader;

    @Value("${filter.mdc.trace.key}")
    private String mdcTraceKey;


    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        try {

            // Handle correlationId first
            String correlationId = request.getHeader(correlationHeader);
            if(correlationId == null || correlationId.isBlank()) {
                correlationId = "CORR-LOS-" + UUID.randomUUID();
                log.warn("Missing tracking header [{}]. Generated fallback correlationId: {}",
                        correlationHeader,
                        correlationId);
            }

            // Handle traceId next
            String traceId = request.getHeader(traceHeader);
            if(traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString().replace("-", "");
                log.warn("Missing tracking header [{}]. Generated standalone traceId: {}",
                        traceHeader,
                        traceId);
            }

            // Add them to MDC
            MDC.put(mdcTraceKey, traceId);
            MDC.put(mdcCorrelationKey, correlationId);

            // Add them to response header
            response.addHeader(correlationHeader, correlationId);
            response.addHeader(traceHeader, traceId);

            // Continue the rest of the business as usual, Chain-of-Command style
            filterChain.doFilter(request, response);

        } finally {
            // Clean up local thread to prevent MDC pollution
            MDC.clear();
        }
    }
}
