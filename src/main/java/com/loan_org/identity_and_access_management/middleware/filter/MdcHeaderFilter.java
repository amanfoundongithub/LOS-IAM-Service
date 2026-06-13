package com.loan_org.identity_and_access_management.middleware.filter;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
public class MdcHeaderFilter extends OncePerRequestFilter {

    @Value("${mdc.header}")
    private String mdcHeader;

    @Value("${mdc.key}")
    private String mdcKey;

    // Logger for logging
    private static final Logger LOGGER = LoggerFactory.getLogger(MdcHeaderFilter.class);

    @Override
    protected void doFilterInternal(@Nonnull HttpServletRequest request,
                                    @Nonnull HttpServletResponse response,
                                    @Nonnull FilterChain filterChain) throws ServletException, IOException {
        try {
            String traceId = request.getHeader(mdcHeader);
            if(traceId == null || traceId.trim().isEmpty()) {
                // Warning: No correlation found
                traceId = UUID.randomUUID().toString();
                LOGGER.warn("No {} header found for request; switching to random traceId: {}. Please consider passing one for traceability.", mdcHeader, traceId);
            }
            MDC.put(mdcKey, traceId);
            response.addHeader(mdcHeader, traceId);
            filterChain.doFilter(request, response);
        } finally {
            MDC.clear();
        }
    }
}
