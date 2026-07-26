package com.loan_org.identity_and_access_management.auth.register.service.impl;

import com.loan_org.identity_and_access_management.auth.register.UserRegistrationRequest;
import com.loan_org.identity_and_access_management.auth.register.UserRegistrationResponse;
import com.loan_org.identity_and_access_management.auth.register.service.RegistrationWorkflowCoordinator;
import com.loan_org.identity_and_access_management.auth.register.service.UserRegistrationFactory;
import com.loan_org.identity_and_access_management.auth.register.service.UserRegistrationService;
import com.loan_org.identity_and_access_management.exception.AccountAlreadyExistsException;
import com.loan_org.identity_and_access_management.userEntity.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private final UserRegistrationFactory userRegistrationFactory;
    private final UserRepository userRepository;
    private final RegistrationWorkflowCoordinator registrationWorkflowCoordinator;

    @Override
    @Transactional
    public UserRegistrationResponse register(UserRegistrationRequest request) {

        log.info(
                "Processing registration request. username={} role={}",
                request.username(),
                request.role()
        );

        validateUniqueIdentifier(
                request.email(),
                request.username()
        );

        UserDocument pendingUser =
                userRegistrationFactory.createPendingUser(request);

        UserDocument savedUser =
                userRepository.save(pendingUser);

        registrationWorkflowCoordinator
                .initiatePostRegistration(savedUser);

        log.info(
                "Registration completed successfully. userId={} username={}",
                savedUser.getId(),
                savedUser.getUsername()
        );

        return UserRegistrationResponse.builder()
                .user(
                    UserResponseDto.builder()
                    .username(savedUser.getUsername())
                    .email(savedUser.getEmail())
                    .attributes(savedUser.getAttributes())
                    .id(savedUser.getId())
                    .status(savedUser.getStatus())
                    .build()
                )
                .build();
    }

    private void validateUniqueIdentifier(
            String email,
            String username) {

        if (userRepository.existsByEmail(email)) {
            throw new AccountAlreadyExistsException(
                    "An account with this email already exists."
            );
        }

        if (userRepository.existsByUsername(username)) {
            throw new AccountAlreadyExistsException(
                    "An account with this username already exists."
            );
        }
    }
}