package com.loan_org.identity_and_access_management.security;

import com.loan_org.identity_and_access_management.dao.RefreshTokenDao;
import com.loan_org.identity_and_access_management.dto.UserResponseDto;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${jwt.secret.key}")
    private String jwtKey;

    @Value("${jwt.metadata.issuer}")
    private String jwtIssuer;

    @Value("${jwt.metadata.expiration_in_minutes}")
    private long jwtExpirationInMinutes;

    @Value("${jwt.refresh_token.expiration_in_days}")
    private long refreshTokenExpirationInDays;

    @Autowired
    private RefreshTokenDao refreshTokenDao;

    private SecretKey signingKey;
    private static final long MINUTES_TO_MILLISECONDS = 60000;

    @PostConstruct
    public void addKeyToClass() {
        signingKey = Keys.hmacShaKeyFor(jwtKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserResponseDto response) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", response.getUsername());
        claims.put("status", response.getStatus());
        claims.put("attributes", response.getAttributes());

        return Jwts.builder()
                .claims(claims)
                .subject(response.getEmail())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationInMinutes * MINUTES_TO_MILLISECONDS))
                .signWith(signingKey)
                .compact();
    }

    public String createRefreshToken() {
        return UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
    }
}
