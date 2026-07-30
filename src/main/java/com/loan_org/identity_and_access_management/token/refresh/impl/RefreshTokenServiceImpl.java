package com.loan_org.identity_and_access_management.token.refresh.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loan_org.identity_and_access_management.auth.login.UserLoginResponse;
import com.loan_org.identity_and_access_management.auth.login.service.JwtService;
import com.loan_org.identity_and_access_management.auth.logout.LogoutRequest;
import com.loan_org.identity_and_access_management.auth.refreshToken.RefreshTokenRequest;
import com.loan_org.identity_and_access_management.exception.TokenNotProvidedException;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import com.loan_org.identity_and_access_management.exception.account.AccountNotFoundException;
import com.loan_org.identity_and_access_management.token.refresh.RefreshTokenDocument;
import com.loan_org.identity_and_access_management.token.refresh.RefreshTokenRepository;
import com.loan_org.identity_and_access_management.token.refresh.RefreshTokenService;
import com.loan_org.identity_and_access_management.userEntity.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenServiceImpl implements RefreshTokenService {
    
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Value("${token.refresh_token.expiry_in_days}")
    private int refreshExpiryDays;

    @Override
    @Transactional
    public String generateRefreshToken(RefreshTokenRequest request) {
        log.info("Processing request for refresh token renewal.");

        if(request.refreshToken() == null) {
            throw new UnauthorizedAccessException("The refresh token is not present. Please re-authenticate.");
        }

        String originalToken = request.refreshToken();
        RefreshTokenDocument document = refreshTokenRepository.findByToken(originalToken)
                .orElseThrow(() -> new TokenNotProvidedException("Provided refresh token is invalid or missing. Verification failed."));

        if (document.isExpired()) {
            refreshTokenRepository.delete(document);
            throw new UnauthorizedAccessException("The refresh token has expired. Please re-authenticate.");
        }

        String newRefreshToken = generateRefreshToken();
        Instant expiry = Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS);

        refreshTokenRepository.deleteByUserEmail(document.getUserEmail());

        RefreshTokenDocument newTokenDocument = new RefreshTokenDocument();
        newTokenDocument.setToken(newRefreshToken);
        newTokenDocument.setUserEmail(document.getUserEmail());
        newTokenDocument.setExpiresAt(expiry);

        refreshTokenRepository.save(newTokenDocument);
        log.info("Successfully cycled refresh token for secure destination context.");

        return newRefreshToken;
    }

    @Override
    @Transactional
    public String generateRefreshToken(String email) {
        log.info("Processing request for issuing of refresh token for associated with email ID: {}",
                email);
        refreshTokenRepository.deleteByUserEmail(email);
        String refreshToken = generateRefreshToken();
        RefreshTokenDocument tokenDocument = RefreshTokenDocument.builder()
                .token(refreshToken)
                .userEmail(email)
                .expiresAt(Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS))
                .build();
        refreshTokenRepository.save(tokenDocument);
        return refreshToken;
    }

    @Override
    @Transactional
    public void revokeRefreshToken(LogoutRequest request) {
        log.info("Processing request for revoking refresh token...");
        Optional<RefreshTokenDocument> tokenDocument = refreshTokenRepository.findByToken(request.refreshToken());
        if(tokenDocument.isEmpty()) {
            log.warn("The given refresh token is not found in record. No active session going on.");
        } else {
            refreshTokenRepository.delete(tokenDocument.get());
            log.info("Successfully revoked refresh token from records.");
        }
    }

    @Override
    @Transactional
    public void revokeRefreshToken(String email) {
        log.info("Processing request for revoking refresh token for given email...");
        refreshTokenRepository.deleteByUserEmail(email);
        log.info("successfully revoked refresh token for given email.");
    }

    @Override
@Transactional
public String loginUsingRefreshToken(RefreshTokenRequest request) {

    log.info("Processing refresh token login request.");

    if (request.refreshToken() == null || request.refreshToken().isBlank()) {
        throw new UnauthorizedAccessException(
                "The refresh token is not present. Please re-authenticate."
        );
    }

    RefreshTokenDocument document = refreshTokenRepository
            .findByToken(request.refreshToken())
            .orElseThrow(() ->
                    new TokenNotProvidedException(
                            "Provided refresh token is invalid or missing."
                    ));

    if (document.isExpired()) {
        refreshTokenRepository.delete(document);
        throw new UnauthorizedAccessException(
                "The refresh token has expired. Please re-authenticate."
        );
    }

    UserDocument user = userRepository.findByEmail(document.getUserEmail())
            .orElseThrow(() ->
                    new AccountNotFoundException(
                            "Associated user account not found."
                    ));

    UserResponseDto userDto = UserResponseDto.builder()
            .id(user.getId())
            .email(user.getEmail())
            .username(user.getUsername())
            .status(user.getStatus())
            .attributes(user.getAttributes())
            .build();

    return jwtService.generateToken(userDto);
}

    private String generateRefreshToken() {
        return UUID.randomUUID() + "-" + UUID.randomUUID();
    }

}
