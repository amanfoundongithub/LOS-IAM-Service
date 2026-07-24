package com.loan_org.identity_and_access_management.auth.service.impl;

import com.loan_org.identity_and_access_management.auth.service.RegistrationWorkflowCoordinator;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.messaging.service.EmailService;
import com.loan_org.identity_and_access_management.token.service.TokenManagementService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationWorkflowCoordinatorImpl implements RegistrationWorkflowCoordinator {

    private final TokenManagementService tokenManagementService;
    private final EmailService           emailService;

    @Override
    public void initiatePostRegistration(UserDocument user) {

        // Generate activation token for the user
        String email = user.getEmail();
        String username = user.getUsername();
        String activationToken = tokenManagementService.generateActivationToken(email);

        // Prevent ghost-email problem
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    emailService.sendActivationEmail(email, username, activationToken);
                }
            });
        } else {
            emailService.sendActivationEmail(email, username, activationToken);
        }

    }
}
