package com.loan_org.identity_and_access_management.middleware.account.impl;

import com.loan_org.identity_and_access_management.exception.AccountLockedException;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.middleware.account.AccountStatusService;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.entity.UserStatus;
import com.loan_org.identity_and_access_management.userEntity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountStatusServiceImpl implements AccountStatusService {

    private final UserRepository userRepository;

    @Override
    public void validate(String email) {

        UserDocument user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "Account not found for email: " + email
                        )
                );

        if (user.getStatus() == UserStatus.LOCKED) {
            throw new AccountLockedException(
                    "Account is locked for email: " + email
            );
        }
    }
}