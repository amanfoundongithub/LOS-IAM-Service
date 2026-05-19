package com.loan_org.identity_and_access_management.service.impl;

import com.loan_org.identity_and_access_management.dao.UserDao;
import com.loan_org.identity_and_access_management.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.entity.MetadataBlock;
import com.loan_org.identity_and_access_management.entity.SecurityBlock;
import com.loan_org.identity_and_access_management.entity.UserDocument;
import com.loan_org.identity_and_access_management.entity.UserStatus;
import com.loan_org.identity_and_access_management.exception.AccountAlreadyExistsException;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import com.loan_org.identity_and_access_management.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;


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
        document.setStatus(UserStatus.ACTIVE);
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
        return userDao.save(document);
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
            security.setLockoutUntil(Instant.now().plusSeconds(15 * 60));
        }
        userDao.save(user);
    }
}
