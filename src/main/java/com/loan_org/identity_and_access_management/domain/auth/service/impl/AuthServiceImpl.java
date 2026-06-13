package com.loan_org.identity_and_access_management.domain.auth.service.impl;

import com.loan_org.identity_and_access_management.domain.user.repository.UserRepository;
import com.loan_org.identity_and_access_management.domain.user.entity.MetadataBlock;
import com.loan_org.identity_and_access_management.domain.user.entity.SecurityBlock;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.domain.user.entity.UserStatus;
import com.loan_org.identity_and_access_management.domain.auth.dto.UserLoginDto;
import com.loan_org.identity_and_access_management.domain.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.domain.user.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.exception.AccountAlreadyExistsException;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import com.loan_org.identity_and_access_management.domain.auth.service.AuthService;
import com.loan_org.identity_and_access_management.messaging.service.EmailService;
import com.loan_org.identity_and_access_management.domain.token.service.TokenManagementService;
import com.loan_org.identity_and_access_management.domain.auth.util.AuthServiceMessageFactory;
import com.loan_org.identity_and_access_management.util.UserAttributeFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Map;

import static java.time.Duration.between;

/**
 * Implementation of the {@link AuthService} providing core Identity and Access Management (IAM)
 * operations, including secure user registration and multi-identifier authentication workflows.
 *
 * <h3>Security and Core Features:</h3>
 * <ul>
 * <li><b>Brute-Force Protection:</b> Implements a time-bound account lockout mechanism
 * after a configurable threshold of consecutive failed login attempts.</li>
 * <li><b>Attribute-Based Access Control (ABAC):</b> Dynamically attaches operational attributes
 * (e.g., signing limits, roles) to the user document during registration for downstream
 * fine-grained authorization.</li>
 * <li><b>Asynchronous Verification:</b> Integrates with token management and email subsystems
 * to dispatch secure registration activation tokens.</li>
 * </ul>
 *
 * <h3>Transaction Management:</h3>
 * This implementation utilizes Spring's transactional boundaries where state mutation and downstream
 * side-effects (such as email dispatch) must maintain atomicity.
 *
 * @author Aman Raj
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    @Value("${user.max_attempts}")
    private int maxAttempts;

    @Value("${user.lockout_minutes}")
    private int lockoutMinutes;

    private static final long MINUTES_TO_SECONDS = 60L;

    // Services injected for the authentication
    private final TokenManagementService tokenManagementService;
    private final EmailService emailService;
    private final UserAttributeFactory userAttributeFactory;

    // User's DAO
    private final UserRepository userRepository;

    // Security helper
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public UserDocument register(UserRegistrationDto registrationData) {

        // Step 1: Validate if there are unique identifiers or not
        String email = registrationData.getEmail();
        String username = registrationData.getUsername();
        hasUniqueIdentifier(email, username);

        // Step 2: Since both are unique, now create an object for the user

        // Step 2.1: Create security block
        String password = registrationData.getPassword();
        SecurityBlock securityBlock = buildSecurityBlock(password);

        // Step 2.2: Create metadata block
        MetadataBlock metadataBlock = buildMetadataBlock();

        // Step 2.3: Create a basic ABAC
        Map<String, Object> attributes = userAttributeFactory.buildRegistrationAttributes(registrationData);

        // Step 2.4: Create the document
        UserDocument userDocument = UserDocument.builder()
                .email(email)
                .username(username)
                .status(UserStatus.PENDING_VERIFICATION)
                .security(securityBlock)
                .metadata(metadataBlock)
                .attributes(attributes)
                .build();

        // Step 3: Save the document in MongoDB and continue post registration stuff
        UserDocument savedUser = userRepository.save(userDocument);
        postRegistrationWorkflow(savedUser);
        return savedUser;
    }

    @Override
    public UserResponseDto login(UserLoginDto loginRequest) {
        UserDocument userDocument = null;

        String email = loginRequest.getEmail();
        String username = loginRequest.getUsername();
        if(email != null && !email.isBlank()) {
            userDocument = userRepository.findByEmail(email)
                    .orElseThrow(() ->
                            new AccountNotFoundException(AuthServiceMessageFactory.emailNotFound(email)));

        }
        else if(username != null && !username.isBlank()) {
            userDocument = userRepository.findByUsername(username)
                    .orElseThrow(() ->
                            new AccountNotFoundException(AuthServiceMessageFactory.usernameNotFound(username)));
        }

        // Authenticate and build response
        String password = loginRequest.getPassword();
        if(userDocument != null) {
            return authenticateAndBuildResponse(userDocument, password);
        } else {
            throw new AccountNotFoundException(AuthServiceMessageFactory.accountNotFound());
        }
    }

    // ---------------------- HELPER FUNCTIONS FOR REGISTRATION ---------------------------

    private void hasUniqueIdentifier(String email, String username) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new AccountAlreadyExistsException(AuthServiceMessageFactory.emailAlreadyExists(email));
        }
        if(userRepository.findByUsername(username).isPresent()) {
            throw new AccountAlreadyExistsException(AuthServiceMessageFactory.usernameAlreadyExists(username));
        }
    }

    private SecurityBlock buildSecurityBlock(String plainPassword) {
        String hashedPassword = passwordEncoder.encode(plainPassword);
        return SecurityBlock.builder()
                .passwordHash(hashedPassword)
                .emailVerified(false)
                .mfaEnabled(false)
                .build();
    }

    private MetadataBlock buildMetadataBlock() {
        return MetadataBlock.builder()
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private void postRegistrationWorkflow(UserDocument userDocument) {
        // Read values
        String email = userDocument.getEmail();
        String username = userDocument.getUsername();

        String tokenString = tokenManagementService.generateActivationToken(email);

        if(TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    emailService.sendActivationEmail(email, username, tokenString);
                }
            });
        } else {
            emailService.sendActivationEmail(email, username, tokenString);
        }
    }

    // ---------------------- HELPER FUNCTIONS FOR LOGIN ---------------------------

    private UserResponseDto authenticateAndBuildResponse(UserDocument document, String password) {

        SecurityBlock block = document.getSecurity();
        if(document.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedAccessException(AuthServiceMessageFactory.accountSuspended());
        }

        if (block.getLockoutUntil() != null && block.getLockoutUntil().isAfter(Instant.now())) {
            long minutesRemaining = between(Instant.now(), block.getLockoutUntil()).toMinutes();
            if(minutesRemaining < 1) {
                long secondsRemaining = between(Instant.now(), block.getLockoutUntil()).toSeconds();
                throw new UnauthorizedAccessException(AuthServiceMessageFactory.accountLocked(secondsRemaining, "seconds"));
            }
            throw new UnauthorizedAccessException(AuthServiceMessageFactory.accountLocked(minutesRemaining, "minutes"));
        }

        if (!passwordEncoder.matches(password, block.getPasswordHash())) {
            int currentAttempts = block.getFailedLoginAttempts() + 1;
            handleFailedLogin(document);
            int leftAttempts = Math.max(0, maxAttempts - currentAttempts);
            throw new UnauthorizedAccessException(AuthServiceMessageFactory.wrongPasswordAttempt(leftAttempts));
        }

        block.setFailedLoginAttempts(0);
        block.setLockoutUntil(null);
        document.getMetadata().setLastLoginAt(Instant.now());
        userRepository.save(document);

        return UserResponseDto.builder()
                .id(document.getId())
                .email(document.getEmail())
                .username(document.getUsername())
                .status(document.getStatus())
                .attributes(document.getAttributes())
                .build();
    }

    private void handleFailedLogin(UserDocument user) {
        SecurityBlock security = user.getSecurity();
        int attempts = security.getFailedLoginAttempts() + 1;
        security.setFailedLoginAttempts(attempts);
        if (attempts >= maxAttempts) {
            security.setLockoutUntil(Instant.now().plusSeconds(lockoutMinutes * MINUTES_TO_SECONDS));
            log.warn("Account with username: {} locked due to incorrect attempts!", user.getUsername());
        }
        userRepository.save(user);
    }
}
