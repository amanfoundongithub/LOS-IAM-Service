package com.loan_org.identity_and_access_management.token.emailActivation.impl;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import com.loan_org.identity_and_access_management.token.emailActivation.EmailActivationTokenDocument;
import com.loan_org.identity_and_access_management.token.emailActivation.EmailActivationTokenRepository;
import com.loan_org.identity_and_access_management.token.emailActivation.EmailActivationTokenService;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.entity.UserStatus;
import com.loan_org.identity_and_access_management.userEntity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailActivationTokenServiceImpl implements EmailActivationTokenService{
    
    private final EmailActivationTokenRepository emailActivationTokenRepository;
    private final UserRepository userRepository;

    @Value("${app.token.activation_token.expiry_in_hours}")
    private int activationExpiryHours;

    @Override
    @Transactional(readOnly = true)
    public String generateActivationToken(String email) {
        log.info("Initiating production validation workflow for activation token generation.");

        String tokenString = generateActivationToken();
        EmailActivationTokenDocument tokenDocument = EmailActivationTokenDocument.builder()
                .token(tokenString)
                .userEmail(email)
                .expiresAt(Instant.now().plus(activationExpiryHours, ChronoUnit.HOURS))
                .build();

        emailActivationTokenRepository.save(tokenDocument);
        log.info("Successfully recorded temporary activation token for user: {}", email);
        return tokenString;
    }

    @Override
    @Transactional 
    public void verifyActivationToken(String activationToken) {
        log.info("Executing transaction synchronization for activation verification.");

        EmailActivationTokenDocument document = emailActivationTokenRepository.findByToken(activationToken)
                .orElseThrow(() -> new UnauthorizedAccessException("The activation token provided is invalid."));

        if (document.isExpired()) {
            emailActivationTokenRepository.delete(document);
            throw new UnauthorizedAccessException("The activation token has expired. Please request a new link.");
        }

        UserDocument userDocument = userRepository.findByEmail(document.getUserEmail())
                .orElseThrow(() -> new AccountNotFoundException("Corrupt data token. No target user matches email: " + document.getUserEmail()));

        userDocument.setStatus(UserStatus.ACTIVE);
        userRepository.save(userDocument);

        emailActivationTokenRepository.delete(document);
        log.info("Account status transitioned to ACTIVE for identifier: {}", document.getUserEmail());
    }


    private String generateActivationToken() {
        return UUID.randomUUID().toString();
    }
}
