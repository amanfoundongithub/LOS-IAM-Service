package com.loan_org.identity_and_access_management.middleware.service.impl;

import com.loan_org.identity_and_access_management.middleware.service.JwtVerificationService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;

@Service
@Slf4j
public class JwtVerificationServiceImpl implements JwtVerificationService {

    @Value("${jwt.secret.key}")
    private String jwtKey;

    private SecretKey verificationKey;

    @PostConstruct
    public void initVerificationKey() {
        this.verificationKey = Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(verificationKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    @Override
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    @SuppressWarnings("unchecked")
    @Override
    public String extractRole(String token) {
        return extractClaim(token, claims -> {
            Map<String, Object> attributes = claims.get("attributes", Map.class);
            if (attributes != null && attributes.containsKey("role")) {
                return (String) attributes.get("role");
            }
            return "GUEST";
        });
    }

    @Override
    public boolean isTokenValid(String token) {
        Date expirationDate = extractClaim(token, Claims::getExpiration);
        try {
            return !expirationDate.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Inbound network token verification rejected: {}", e.getMessage());
            return false;
        }
    }
}
