package com.loan_org.identity_and_access_management.token.passwordReset.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import com.loan_org.identity_and_access_management.messaging.service.EmailService;
import com.loan_org.identity_and_access_management.token.passwordReset.PasswordResetTokenDocument;
import com.loan_org.identity_and_access_management.token.passwordReset.PasswordResetTokenRepository;
import com.loan_org.identity_and_access_management.token.passwordReset.PasswordResetTokenService;
import com.loan_org.identity_and_access_management.userEntity.entity.SecurityBlock;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetTokenServiceImpl implements PasswordResetTokenService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${token.password_reset_token.expiry_in_hours}")
    private int resetExpiryHours;

    @Override
    @Transactional
    public void generatePasswordResetToken(String email) {
        log.info("Validating baseline identity for password reset generation sequence.");

        UserDocument userDocument = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("No account linked to email destination: " + email));

        String tokenString = generatePasswordResetToken();
        PasswordResetTokenDocument tokenDocument = PasswordResetTokenDocument.builder()
                .token(tokenString)
                .expiresAt(Instant.now().plus(resetExpiryHours, ChronoUnit.HOURS))
                .build();
        passwordResetTokenRepository.save(tokenDocument);

        // Dispatches to your decoupled, non-blocking @Async thread layout
        emailService.sendPasswordResetEmail(email, userDocument.getUsername(), tokenString);

        log.info("Successfully dispatched secure reset link generation to destination account: {}", email);
    }

    @Override
    @Transactional
    public void verifyPasswordResetToken(String passwordResetToken, String newPassword) {
        log.info("Executing secure transaction validation verification for reset verification.");

        PasswordResetTokenDocument document = passwordResetTokenRepository.findByToken(passwordResetToken)
                .orElseThrow(() -> new UnauthorizedAccessException("The credentials token provided is invalid."));

        if (document.isExpired()) {
            passwordResetTokenRepository.delete(document);
            throw new UnauthorizedAccessException("The password reset token has expired. Please initiate request again.");
        }

        UserDocument userDocument = userRepository.findByEmail(document.getUserEmail())
                .orElseThrow(() -> new AccountNotFoundException("No target user account matches token identifier: " + document.getUserEmail()));

        SecurityBlock securityBlock = userDocument.getSecurity();
        securityBlock.setPasswordHash(passwordEncoder.encode(newPassword));
        userDocument.setSecurity(securityBlock);

        userRepository.save(userDocument);
        passwordResetTokenRepository.delete(document);

        log.info("Credentials security block hashed and updated for user destination: {}", document.getUserEmail());
    }

    private String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }
}
