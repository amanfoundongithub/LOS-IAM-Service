package com.loan_org.identity_and_access_management.jwt.impl;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.loan_org.identity_and_access_management.jwt.JwtUserClaims;
import com.loan_org.identity_and_access_management.jwt.JwtVerificationService;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

@Service
@Slf4j
public class JwtVerificationServiceImpl implements JwtVerificationService {

    // Define the keys for accessing JWT Claims
    private static final String ATTRIBUTES = "attributes";
    private static final String USER_ROLE = "user_role";

    // Define the JWT secret key from yaml
    @Value("${jwt.secret.key}")
    private String jwtKey;

    private SecretKey verificationKey;

    @PostConstruct
    public void initVerificationKey() {
        this.verificationKey = Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    @SuppressWarnings("unchecked")
    public JwtUserClaims verify(String token) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            if(expiration == null) {
                throw new JwtException("JWT missing expiration");
            }
            Instant expirationInstant = expiration.toInstant();
            Instant now = Instant.now();
            if (expirationInstant.isBefore(now)) {
                throw new JwtException("JWT expired");
            }
            Map<String, Object> attributes =
                    claims.get(ATTRIBUTES, Map.class);
            if (attributes == null) {
                throw new JwtException("JWT missing attributes claim");
            }
            String role = (String) attributes.get(USER_ROLE);
            if (role == null || role.isBlank()) {
                throw new JwtException("Missing role claim");
            }
            String email = claims.getSubject();
            if (email == null || email.isBlank()) {
                throw new JwtException("JWT missing subject");
            }
            return new JwtUserClaims(
                email,
                attributes
            );
        } catch (JwtException | IllegalArgumentException ex) {
            log.warn("JWT verification failed: {}", ex.getMessage());
            throw ex;
        }
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(verificationKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
