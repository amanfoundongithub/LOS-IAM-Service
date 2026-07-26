package com.loan_org.identity_and_access_management.auth.register.service.impl;

import com.loan_org.identity_and_access_management.auth.register.service.RegistrationWorkflowCoordinator;
import com.loan_org.identity_and_access_management.messaging.service.EmailService;
import com.loan_org.identity_and_access_management.token.service.TokenManagementService;
import com.loan_org.identity_and_access_management.user.entity.UserDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationWorkflowCoordinatorImpl
        implements RegistrationWorkflowCoordinator {

    private final TokenManagementService tokenManagementService;
    private final EmailService emailService;

    @Override
    public void initiatePostRegistration(UserDocument user) {

        String email = user.getEmail();
        String username = user.getUsername();

        log.debug("Generating activation token for user [{}].", email);

        String activationToken =
                tokenManagementService.generateActivationToken(email);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            sendActivationEmail(email, username, activationToken);
                        }
                    });

        } else {

            sendActivationEmail(email, username, activationToken);

        }
    }

    private void sendActivationEmail(
            String email,
            String username,
            String activationToken) {

        log.info("Sending activation email to [{}].", email);

        emailService.sendActivationEmail(
                email,
                username,
                activationToken
        );
    }
}