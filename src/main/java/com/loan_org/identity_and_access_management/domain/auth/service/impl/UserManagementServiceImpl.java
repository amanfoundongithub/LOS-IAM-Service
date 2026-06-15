package com.loan_org.identity_and_access_management.domain.auth.service.impl;

import com.loan_org.identity_and_access_management.domain.auth.dto.PasswordChangeRequestDto;
import com.loan_org.identity_and_access_management.domain.auth.service.UserManagementService;
import com.loan_org.identity_and_access_management.domain.token.service.TokenManagementService;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.domain.user.repository.UserRepository;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserManagementServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final TokenManagementService tokenManagementService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void updatePassword(String email, PasswordChangeRequestDto changeRequest) {
        log.info("Received request to update password for email: {}",
                email);

        UserDocument userDocument = userRepository.findByEmail(email)
                .orElseThrow(() -> new AccountNotFoundException("No account found for user with email: " + email));

        String hashFromDB = userDocument.getSecurity().getPasswordHash();
        if(!passwordEncoder.matches(changeRequest.oldPassword(), hashFromDB)) {
            throw new UnauthorizedAccessException("Invalid password for change. Please enter correct password.");
        }
        String hashedPassword = passwordEncoder.encode(changeRequest.newPassword());
        userDocument.getSecurity().setPasswordHash(hashedPassword);
        log.info("Successfully updated the password for email: {}",
                email);
        userRepository.save(userDocument);
        tokenManagementService.revokeRefreshToken(email);
    }
}
