package com.loan_org.identity_and_access_management.auth.login.service.impl;

import com.loan_org.identity_and_access_management.auth.login.service.UserLoginSecurityEvaluatorService;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import com.loan_org.identity_and_access_management.userEntity.entity.SecurityBlock;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.entity.UserStatus;
import com.loan_org.identity_and_access_management.userEntity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserLoginSecurityEvaluatorServiceImpl implements UserLoginSecurityEvaluatorService {

    @Value("${login.max_attempt}")
    private int maxAttempts;

    @Value("${login.lockout_in_minutes}")
    private int lockoutInMinutes;

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserRepository        userRepository;

    @Override
    public void verifyAccountPolicies(UserDocument document) {

        // Suspended account?
        if (document.getStatus() == UserStatus.LOCKED) {
            throw new UnauthorizedAccessException("Account is suspended");
        }

        // Check for security lockout and raise error if account is locked
        SecurityBlock security = document.getSecurity();
        if (checkIfSecurityLockoutEnabled(security)) {
            TimeLeftWithUnit leftTime = calculateTimeLeftWithUnits(security);
            throw new UnauthorizedAccessException(
                    "Lots of time left"
            );
        }

    }

    @Override
    public void verifyCredentials(UserDocument document, String plainPassword) {
        SecurityBlock security = document.getSecurity();
        if (!passwordEncoder.matches(plainPassword, security.getPasswordHash())) {
            int currentAttempts = security.getFailedLoginAttempts() + 1;
            handleFailedAttempt(document, currentAttempts);
            int attemptsLeft = Math.max(0, maxAttempts - currentAttempts);
            throw new UnauthorizedAccessException("Wrong password");
        }
    }

    // --------- HELPER ----------
    private boolean checkIfSecurityLockoutEnabled(SecurityBlock security) {
        if (security.getLockoutUntil() == null) {
            return false;
        }
        return security.getLockoutUntil().isAfter(Instant.now());
    }

    private TimeLeftWithUnit calculateTimeLeftWithUnits(SecurityBlock security) {
        Duration remainingDuration = Duration.between(Instant.now(), security.getLockoutUntil());
        long minutesRemaining = remainingDuration.toMinutes();
        if (minutesRemaining >= 1) {
            return new TimeLeftWithUnit(minutesRemaining, "minutes");
        } else {
            long secondsRemaining = Math.max(0, remainingDuration.toSeconds());
            return new TimeLeftWithUnit(secondsRemaining, "seconds");
        }
    }

    private void handleFailedAttempt(UserDocument user, int attempts) {
        SecurityBlock security = user.getSecurity();
        security.setFailedLoginAttempts(attempts);
        if (attempts >= maxAttempts) {
            security.setLockoutUntil(Instant.now().plusSeconds(lockoutInMinutes * 60L));
            log.warn("Account linked to username [{}] has been locked due to consecutive authentication failures.", user.getUsername());
        }
        userRepository.save(user);
    }

    private record TimeLeftWithUnit(
            long timeLeft,
            String unit
    ) {}
}