package com.loan_org.identity_and_access_management.domain.auth.service.impl;

import com.loan_org.identity_and_access_management.domain.auth.factory.UserRegistrationFactory;
import com.loan_org.identity_and_access_management.domain.auth.service.LoginSecurityEvaluatorService;
import com.loan_org.identity_and_access_management.domain.auth.service.RegistrationWorkflowCoordinator;
import com.loan_org.identity_and_access_management.domain.user.repository.UserRepository;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.domain.auth.dto.UserLoginDto;
import com.loan_org.identity_and_access_management.domain.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.domain.user.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.exception.AccountAlreadyExistsException;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.domain.auth.service.AuthService;
import com.loan_org.identity_and_access_management.domain.auth.util.AuthServiceMessageFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    // Services injected for the authentication
    private final UserRegistrationFactory         userRegistrationFactory;
    private final RegistrationWorkflowCoordinator registrationWorkflowCoordinator;
    private final LoginSecurityEvaluatorService   loginSecurityEvaluatorService;
    private final UserRepository                  userRepository;

    @Override
    @Transactional
    public UserDocument register(UserRegistrationDto registrationData) {

        log.info("Received request for creating account for new {} with username {}.",
                registrationData.getRole(),
                registrationData.getUsername());

        log.info("Starting check for existing account...");
        hasUniqueIdentifier(registrationData.getEmail(), registrationData.getUsername());

        // Do the workflow as needed
        log.info("No existing account is found. Creating the account now...");
        UserDocument newEntity = userRegistrationFactory.createPendingUser(registrationData);
        UserDocument savedEntity = userRepository.save(newEntity);
        registrationWorkflowCoordinator.initiatePostRegistration(savedEntity);
        log.info("Account has been created successfully for email: {} and username: {}",
                savedEntity.getEmail(),
                savedEntity.getUsername());
        return savedEntity;
    }

    @Override
    @Transactional
    public UserResponseDto login(UserLoginDto loginRequest) {

        log.info("Received request for login for user. Finding user in DB...");
        UserDocument userDocument = findUser(loginRequest);

        log.info("User document is found. Performing account policy checks...");
        loginSecurityEvaluatorService.verifyAccountPolicies(userDocument);

        log.info("Policy checks successful. Now verifying credentials...");
        loginSecurityEvaluatorService.verifyCredentials(userDocument, loginRequest.getPassword());

        log.info("User is successfully verified. LOGIN OK");
        userDocument.getSecurity().setFailedLoginAttempts(0);
        userDocument.getSecurity().setLockoutUntil(null);
        userDocument.getMetadata().setLastLoginAt(Instant.now());
        userRepository.save(userDocument);

        return UserResponseDto.builder()
                .id(userDocument.getId())
                .email(userDocument.getEmail())
                .username(userDocument.getUsername())
                .status(userDocument.getStatus())
                .attributes(userDocument.getAttributes())
                .build();
    }

    // ---------- HELPERS ------------
    private void hasUniqueIdentifier(String email, String username) {
        if(userRepository.findByEmail(email).isPresent()) {
            throw new AccountAlreadyExistsException(AuthServiceMessageFactory.emailAlreadyExists(email));
        }
        if(userRepository.findByUsername(username).isPresent()) {
            throw new AccountAlreadyExistsException(AuthServiceMessageFactory.usernameAlreadyExists(username));
        }
    }

    private UserDocument findUser(UserLoginDto request) {
        if (request.getEmail() != null && !request.getEmail().isBlank()) {
            return userRepository.findByEmail(request.getEmail())
                    .orElseThrow(() -> new AccountNotFoundException(AuthServiceMessageFactory.emailNotFound(request.getEmail())));
        }
        if (request.getUsername() != null && !request.getUsername().isBlank()) {
            return userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new AccountNotFoundException(AuthServiceMessageFactory.usernameNotFound(request.getUsername())));
        }
        throw new AccountNotFoundException(AuthServiceMessageFactory.accountNotFound());
    }

}