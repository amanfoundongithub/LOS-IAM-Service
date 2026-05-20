package com.loan_org.identity_and_access_management.service.impl;

import com.loan_org.identity_and_access_management.dao.UserDao;
import com.loan_org.identity_and_access_management.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.entity.*;
import com.loan_org.identity_and_access_management.exception.AccountAlreadyExistsException;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import com.loan_org.identity_and_access_management.service.AuthService;
import com.loan_org.identity_and_access_management.service.EmailService;
import com.loan_org.identity_and_access_management.service.TokenManagementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    // Services for the authentication
    private final TokenManagementService tokenManagementService;
    private final EmailService emailService;

    // DAO
    private final UserDao userDao;

    // Security helper
    private final BCryptPasswordEncoder passwordEncoder;


    @Override
    public UserDocument register(UserRegistrationDto registrationData) {

        if(userDao.findByEmail(registrationData.getEmail()).isPresent()) {
            throw new AccountAlreadyExistsException("An account with email : " + registrationData.getEmail()
                    + "already exists! Please try a different account");
        }

        if(userDao.findByUsername(registrationData.getUsername()).isPresent()) {
            throw new AccountAlreadyExistsException("An account with username : " + registrationData.getUsername()
                    + "already exists! Please try a different account");
        }

        UserDocument document  = new UserDocument();
        document.setEmail(registrationData.getEmail());
        document.setStatus(UserStatus.PENDING_VERIFICATION);
        document.setUsername(registrationData.getUsername());

        // Security
        SecurityBlock security = new SecurityBlock();
        security.setPasswordHash(passwordEncoder.encode(registrationData.getPassword()));
        security.setEmailVerified(true);
        security.setMfaEnabled(false);
        document.setSecurity(security);

        // ABAC
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("max_approval_limit_inr", registrationData.getSigningLimit());
        attributes.put("user_role", registrationData.getRole());
        document.setAttributes(attributes);

        // Metadata
        MetadataBlock metadata = new MetadataBlock();
        metadata.setCreatedAt(Instant.now());
        metadata.setUpdatedAt(Instant.now());
        document.setMetadata(metadata);

        // Save
        UserDocument savedUser = userDao.save(document);

        // Create & Send token
        String tokenString = tokenManagementService.generateActivationToken(document.getEmail());
        emailService.sendActivationEmail(savedUser.getEmail(), savedUser.getUsername(), tokenString);

        return savedUser;
    }

    @Override
    public UserResponseDto loginWithEmail(String email, String password) {
        UserDocument document = userDao.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("No account found for:" + email));

        return authenticateAndBuildResponse(document, password);
    }

    @Override
    public UserResponseDto loginWithUsername(String username, String password) {
        UserDocument document = userDao.findByUsername(username)
                .orElseThrow(() -> new AccountNotFoundException("No account found for:" + username));

        return authenticateAndBuildResponse(document, password);
    }

    private UserResponseDto authenticateAndBuildResponse(UserDocument document, String password) {
        SecurityBlock block = document.getSecurity();
        if(document.getStatus() == UserStatus.SUSPENDED) {
            throw new UnauthorizedAccessException("This account is suspended, Please contact the admin");
        }

        if (block.getLockoutUntil() != null && block.getLockoutUntil().isAfter(Instant.now())) {
            throw new UnauthorizedAccessException("Account is temporarily locked. Try again later.");
        }

        if (!passwordEncoder.matches(password, block.getPasswordHash())) {
            handleFailedLogin(document);
            throw new UnauthorizedAccessException("Invalid credentials provided.");
        }

        block.setFailedLoginAttempts(0);
        block.setLockoutUntil(null);
        document.getMetadata().setLastLoginAt(Instant.now());
        userDao.save(document);

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

        if (attempts >= 5) {
            security.setLockoutUntil(Instant.now().plusSeconds(15 * 60L));
        }
        userDao.save(user);
    }
}
