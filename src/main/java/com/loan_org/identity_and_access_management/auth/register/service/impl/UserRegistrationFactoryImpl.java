package com.loan_org.identity_and_access_management.auth.register.service.impl;

import com.loan_org.identity_and_access_management.auth.register.UserRegistrationRequest;
import com.loan_org.identity_and_access_management.auth.register.service.UserRegistrationFactory;
import com.loan_org.identity_and_access_management.userEntity.entity.MetadataBlock;
import com.loan_org.identity_and_access_management.userEntity.entity.SecurityBlock;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.entity.UserStatus;
import com.loan_org.identity_and_access_management.userEntity.factory.UserAttributeFactory;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserRegistrationFactoryImpl implements UserRegistrationFactory {

    private final BCryptPasswordEncoder passwordEncoder;
    private final UserAttributeFactory  attributeFactory;

    @Override
    public UserDocument createPendingUser(UserRegistrationRequest registrationDetails) {
        return UserDocument.builder()
                .email(registrationDetails.email())
                .username(registrationDetails.username())
                .status(UserStatus.PENDING_VERIFICATION)
                .security(buildSecurityBlock(registrationDetails.password()))
                .metadata(buildMetadataBlock())
                .attributes(buildAttributes(registrationDetails))
                .build();
    }

    private SecurityBlock buildSecurityBlock(String password) {
        return SecurityBlock.builder()
                .passwordHash(passwordEncoder.encode(password))
                .emailVerified(false)
                .mfaEnabled(false)
                .failedLoginAttempts(0)
                .build();
    }

    private MetadataBlock buildMetadataBlock() {
        return MetadataBlock.builder()
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
    }

    private Map<String, Object> buildAttributes(UserRegistrationRequest dto) {
        return attributeFactory.getAttributes(dto.role());
    }
}
