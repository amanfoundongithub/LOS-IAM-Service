package com.loan_org.identity_and_access_management.middleware.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.loan_org.identity_and_access_management.middleware.UserPrincipal;
import com.loan_org.identity_and_access_management.middleware.account.AccountStatusService;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class AccountStatusFilter extends OncePerRequestFilter {

    private final AccountStatusService accountStatusService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            filterChain.doFilter(request, response);
            return;
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        accountStatusService.validate(principal.email());
        filterChain.doFilter(request, response);
    }
}