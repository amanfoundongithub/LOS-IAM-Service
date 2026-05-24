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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of the TokenCreationService class
 *
 * @author Aman Raj
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TokenManagementServiceImpl implements TokenManagementService {

    // Data access objects for database interactions
    private final UserDao userDao;
    private final RefreshTokenDao refreshTokenDao;
    private final ActivationTokenDao activationTokenDao;
    private final PasswordResetTokenDao passwordResetTokenDao;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    // Service to create tokens
    private final JwtService jwtService;

    @Override
    public String generateRefreshToken(RefreshTokenRequestDto request) {
        log.info("Received request for REFRESH TOKEN RENEWAL");
        // Find in DB, if not throw an exception
        String originalToken = request.getRefreshToken();
        Optional<RefreshTokenDocument> refreshTokenDocument = refreshTokenDao.findByToken(originalToken);
        if(refreshTokenDocument.isEmpty()) {
            throw new TokenNotProvidedException("No refresh token has been provided. Please provide one for" +
                    " verification.");
        }
        RefreshTokenDocument document = refreshTokenDocument.get();

        // Check for token's expiry, if not request for login
        if(document.isExpired()) {
            throw new UnauthorizedAccessException("The refresh token has EXPIRED! Please try to login again" +
                    " for the same.");
        }

        // All checks passed now generate a fresh one
        String newRefreshToken = jwtService.createRefreshToken();
        Instant expiry = Instant.now().plus(24, ChronoUnit.DAYS);

        // Add to the database
        refreshTokenDao.deleteByUserEmail(document.getUserEmail());

        RefreshTokenDocument newTokenDocument = new RefreshTokenDocument();
        newTokenDocument.setToken(newRefreshToken);
        newTokenDocument.setUserEmail(document.getUserEmail());
        newTokenDocument.setExpiresAt(expiry);

        refreshTokenDao.save(newTokenDocument);

        log.info("Successful Generation of New Refresh Token!");
        return newRefreshToken;
    }

    @Override
    public String generateActivationToken(String email) {
        log.info("Received request for Activation Token Creation");
        // Check if email is there or not
        Optional<UserDocument> userDocument = userDao.findByEmail(email);
        if(userDocument.isEmpty()) {
            throw new AccountNotFoundException("No account found with email: " + email + "!");
        }

        // Create new token
        String tokenString = UUID.randomUUID().toString();
        ActivationTokenDocument tokenDocument = new ActivationTokenDocument(
            tokenString, email, 24
        );
        activationTokenDao.save(tokenDocument);
        log.info("Successfully created activation token for: {} for 24 hours!", email);
        return tokenString;
    }

    @Override
    public void verifyActivationToken(String activationToken) {
        log.info("Verification Request for Activation Token!");
        Optional<ActivationTokenDocument> optionalDocument = activationTokenDao.findByToken(activationToken);
        if(optionalDocument.isEmpty()) {
            throw new UnauthorizedAccessException("Invalid activation token");
        }
        ActivationTokenDocument document = optionalDocument.get();
        if(document.isExpired()) {
            activationTokenDao.delete(document);
            throw new UnauthorizedAccessException("Token expired, please generate a new one!");
        }
        Optional<UserDocument> optionalUserDocument = userDao.findByEmail(document.getUserEmail());
        if(optionalUserDocument.isEmpty()) {
            throw new AccountNotFoundException("No account found with email: " + document.getUserEmail());
        }
        UserDocument userDocument = optionalUserDocument.get();
        userDocument.setStatus(UserStatus.ACTIVE);
        userDao.save(userDocument);
        activationTokenDao.delete(document);
    }

    @Override
    public String generatePasswordResetToken(String email) {
        log.info("Received request for Password Reset Token Creation");
        // Check if email is there or not
        Optional<UserDocument> userDocument = userDao.findByEmail(email);
        if(userDocument.isEmpty()) {
            throw new AccountNotFoundException("No account found with email: " + email + "!");
        }

        // Create new token
        String tokenString = UUID.randomUUID().toString();
        PasswordResetTokenDocument tokenDocument = new PasswordResetTokenDocument(
                tokenString, email, 24
        );
        passwordResetTokenDao.save(tokenDocument);

        // Send email
        emailService.sendPasswordResetEmail(email, userDocument.get().getUsername(), tokenString);

        log.info("Successfully created password reset token for: {} for 24 hours!", email);
        return tokenString;
    }

    @Override
    public void verifyPasswordResetToken(String passwordResetToken, String newPassword) {
        log.info("Verification Request for Password Reset Token!");
        Optional<PasswordResetTokenDocument> optionalDocument = passwordResetTokenDao.findByToken(passwordResetToken);
        if(optionalDocument.isEmpty()) {
            throw new UnauthorizedAccessException("Invalid activation token");
        }
        PasswordResetTokenDocument document = optionalDocument.get();
        if(document.isExpired()) {
            passwordResetTokenDao.delete(document);
            throw new UnauthorizedAccessException("Token expired, please generate a new one!");
        }
        Optional<UserDocument> optionalUserDocument = userDao.findByEmail(document.getUserEmail());
        if(optionalUserDocument.isEmpty()) {
            throw new AccountNotFoundException("No account found with email: " + document.getUserEmail());
        }
        UserDocument userDocument = optionalUserDocument.get();
        SecurityBlock securityBlock = userDocument.getSecurity();
        securityBlock.setPasswordHash(passwordEncoder.encode(newPassword));
        userDocument.setSecurity(securityBlock);
        userDao.save(userDocument);
        passwordResetTokenDao.delete(document);
    }


}
