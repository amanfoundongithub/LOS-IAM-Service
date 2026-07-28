package com.loan_org.identity_and_access_management.auth.login.service.impl;

import com.loan_org.identity_and_access_management.auth.login.UserLoginRequest;
import com.loan_org.identity_and_access_management.auth.login.UserLoginResponse;
import com.loan_org.identity_and_access_management.auth.login.service.JwtService;
import com.loan_org.identity_and_access_management.auth.login.service.UserLoginSecurityEvaluatorService;
import com.loan_org.identity_and_access_management.auth.login.service.UserLoginService;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.token.refresh.RefreshTokenService;
import com.loan_org.identity_and_access_management.userEntity.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserLoginServiceImpl implements UserLoginService {

    private final UserLoginSecurityEvaluatorService loginSecurityEvaluatorService;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    @Override
    @Transactional
    public UserLoginResponse login(UserLoginRequest loginRequest) {

        log.info("Processing login request.");

        UserDocument userDocument = findUser(loginRequest);

        loginSecurityEvaluatorService.verifyAccountPolicies(userDocument);
        loginSecurityEvaluatorService.verifyCredentials(
                userDocument,
                loginRequest.password()
        );

        userDocument.getSecurity().setFailedLoginAttempts(0);
        userDocument.getSecurity().setLockoutUntil(null);
        userDocument.getMetadata().setLastLoginAt(Instant.now());

        userRepository.save(userDocument);

        UserResponseDto user =
                UserResponseDto.builder()
                        .id(userDocument.getId())
                        .email(userDocument.getEmail())
                        .username(userDocument.getUsername())
                        .status(userDocument.getStatus())
                        .attributes(userDocument.getAttributes())
                        .build();

        String accessToken = jwtService.generateToken(user);
        String refreshToken = refreshTokenService.generateRefreshToken(userDocument.getEmail());

        log.info("User [{}] authenticated successfully.", user.email());

        return new UserLoginResponse(
                accessToken,
                refreshToken,
                user
        );
    }

    private UserDocument findUser(UserLoginRequest request) {

        return userRepository
                .findByEmailOrUsername(request.email(), request.username())
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Invalid username/email or password."
                        ));
    }
}