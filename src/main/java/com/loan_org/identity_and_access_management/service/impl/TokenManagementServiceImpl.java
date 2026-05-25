package com.loan_org.identity_and_access_management.service.impl;

import com.loan_org.identity_and_access_management.dao.ActivationTokenDao;
import com.loan_org.identity_and_access_management.dao.PasswordResetTokenDao;
import com.loan_org.identity_and_access_management.dao.RefreshTokenDao;
import com.loan_org.identity_and_access_management.dao.UserDao;
import com.loan_org.identity_and_access_management.dto.RefreshTokenRequestDto;
import com.loan_org.identity_and_access_management.entity.*;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.exception.TokenNotProvidedException;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import com.loan_org.identity_and_access_management.security.JwtService;
import com.loan_org.identity_and_access_management.service.EmailService;
import com.loan_org.identity_and_access_management.service.TokenManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

/**
 * Enterprise implementation of the {@link TokenManagementService} responsible for the atomic
 * orchestration of verification, secure storage, and generation lifecycle of infrastructure tokens.
 *
 * @author Aman Raj
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenManagementServiceImpl implements TokenManagementService {

    private final UserDao userDao;
    private final RefreshTokenDao refreshTokenDao;
    private final ActivationTokenDao activationTokenDao;
    private final PasswordResetTokenDao passwordResetTokenDao;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Value("${app.token.refresh_token.expiry_in_days}")
    private int refreshExpiryDays;

    @Value("${app.token.activation_token.expiry_in_hours}")
    private int activationExpiryHours;

    @Value("${app.token.reset_token.expiry_in_hours}")
    private int resetExpiryHours;

    @Override
    @Transactional
    public String generateRefreshToken(RefreshTokenRequestDto request) {
        log.info("Processing request for refresh token renewal.");

        String originalToken = request.getRefreshToken();
        RefreshTokenDocument document = refreshTokenDao.findByToken(originalToken)
                .orElseThrow(() -> new TokenNotProvidedException("Provided refresh token is invalid or missing. Verification failed."));

        if (document.isExpired()) {
            refreshTokenDao.delete(document);
            throw new UnauthorizedAccessException("The refresh token has expired. Please re-authenticate.");
        }

        String newRefreshToken = jwtService.createRefreshToken();
        Instant expiry = Instant.now().plus(refreshExpiryDays, ChronoUnit.DAYS);

        // Atomic cycle prevents multi-node cluster collisions
        refreshTokenDao.deleteByUserEmail(document.getUserEmail());

        RefreshTokenDocument newTokenDocument = new RefreshTokenDocument();
        newTokenDocument.setToken(newRefreshToken);
        newTokenDocument.setUserEmail(document.getUserEmail());
        newTokenDocument.setExpiresAt(expiry);

        refreshTokenDao.save(newTokenDocument);
        log.info("Successfully cycled refresh token for secure destination context.");

        return newRefreshToken;
    }

    @Override
    @Transactional(readOnly = true)
    public String generateActivationToken(String email) {
        log.info("Initiating production validation workflow for activation token generation.");

        userDao.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("No account linked to email destination: " + email));

        String tokenString = UUID.randomUUID().toString();
        ActivationTokenDocument tokenDocument = new ActivationTokenDocument(
                tokenString, email, activationExpiryHours
        );

        activationTokenDao.save(tokenDocument);
        log.info("Successfully recorded temporary activation token for user: {}", email);

        return tokenString;
    }

    @Override
    @Transactional // Critical: Multi-table mutation operations require atomic transactional guarantees
    public void verifyActivationToken(String activationToken) {
        log.info("Executing transaction synchronization for activation verification.");

        ActivationTokenDocument document = activationTokenDao.findByToken(activationToken)
                .orElseThrow(() -> new UnauthorizedAccessException("The activation token provided is invalid."));

        if (document.isExpired()) {
            activationTokenDao.delete(document);
            throw new UnauthorizedAccessException("The activation token has expired. Please request a new link.");
        }

        UserDocument userDocument = userDao.findByEmail(document.getUserEmail())
                .orElseThrow(() -> new AccountNotFoundException("Corrupt data token. No target user matches email: " + document.getUserEmail()));

        userDocument.setStatus(UserStatus.ACTIVE);
        userDao.save(userDocument);

        // Remove token only after user state transitions cleanly
        activationTokenDao.delete(document);
        log.info("Account status transitioned to ACTIVE for identifier: {}", document.getUserEmail());
    }

    @Override
    @Transactional
    public String generatePasswordResetToken(String email) {
        log.info("Validating baseline identity for password reset generation sequence.");

        UserDocument userDocument = userDao.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("No account linked to email destination: " + email));

        String tokenString = UUID.randomUUID().toString();
        PasswordResetTokenDocument tokenDocument = new PasswordResetTokenDocument(
                tokenString, email, resetExpiryHours
        );
        passwordResetTokenDao.save(tokenDocument);

        // Dispatches to your decoupled, non-blocking @Async thread layout
        emailService.sendPasswordResetEmail(email, userDocument.getUsername(), tokenString);

        log.info("Successfully dispatched secure reset link generation to destination account: {}", email);
        return tokenString;
    }

    @Override
    @Transactional
    public void verifyPasswordResetToken(String passwordResetToken, String newPassword) {
        log.info("Executing secure transaction validation verification for reset verification.");

        PasswordResetTokenDocument document = passwordResetTokenDao.findByToken(passwordResetToken)
                .orElseThrow(() -> new UnauthorizedAccessException("The credentials token provided is invalid."));

        if (document.isExpired()) {
            passwordResetTokenDao.delete(document);
            throw new UnauthorizedAccessException("The password reset token has expired. Please initiate request again.");
        }

        UserDocument userDocument = userDao.findByEmail(document.getUserEmail())
                .orElseThrow(() -> new AccountNotFoundException("No target user account matches token identifier: " + document.getUserEmail()));

        SecurityBlock securityBlock = userDocument.getSecurity();
        securityBlock.setPasswordHash(passwordEncoder.encode(newPassword));
        userDocument.setSecurity(securityBlock);

        userDao.save(userDocument);
        passwordResetTokenDao.delete(document);

        log.info("Credentials security block hashed and updated for user destination: {}", document.getUserEmail());
    }
}