package com.loan_org.identity_and_access_management.auth.service.impl;

import com.loan_org.identity_and_access_management.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.auth.factory.UserRegistrationFactory;
import com.loan_org.identity_and_access_management.auth.login.UserLoginRequest;
import com.loan_org.identity_and_access_management.auth.login.service.UserLoginSecurityEvaluatorService;
import com.loan_org.identity_and_access_management.auth.service.AuthService;
import com.loan_org.identity_and_access_management.auth.service.RegistrationWorkflowCoordinator;
import com.loan_org.identity_and_access_management.auth.util.AuthServiceMessageFactory;
import com.loan_org.identity_and_access_management.exception.AccountAlreadyExistsException;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.user.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.user.repository.UserRepository;

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
    private final UserLoginSecurityEvaluatorService   loginSecurityEvaluatorService;
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

    private UserDocument findUser(UserLoginRequest request) {
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