package com.loan_org.identity_and_access_management.filters;

import com.loan_org.identity_and_access_management.jwt.JwtUserClaims;
import com.loan_org.identity_and_access_management.jwt.JwtVerificationService;
import com.loan_org.identity_and_access_management.security.UserPrincipal;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String USER_ROLE = "user_role";

    private final String baseUrl;
    private final JwtVerificationService jwtVerificationService;

    public JwtAuthenticationFilter(
            JwtVerificationService jwtVerificationService,
            @Value("${api.auth.base_url}") String baseUrl) {

        this.jwtVerificationService = jwtVerificationService;
        this.baseUrl = baseUrl;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {

            String jwt = authHeader.substring(7);

            JwtUserClaims claims = jwtVerificationService.verify(jwt);
            
            Map<String, Object> attributes = claims.attributes();
            String role = (String) attributes.get(USER_ROLE);

            UserPrincipal principal = new UserPrincipal(
                    claims.email(),
                    role
            );

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            principal,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );

            authentication.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            MDC.put("email", claims.email());
            MDC.put("role", role);
            filterChain.doFilter(request, response);
        } catch (JwtException ex) {
            throw new BadCredentialsException("Invalid JWT", ex);
        }
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {

        String path = request.getRequestURI();

        return path.equals(baseUrl + "/login")
                || path.equals(baseUrl + "/register")
                || path.equals(baseUrl + "/refresh")
                || path.equals(baseUrl + "/verify")
                || path.equals(baseUrl + "/reset-password-request");
    }
}