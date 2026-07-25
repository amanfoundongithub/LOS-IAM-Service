package com.loan_org.identity_and_access_management.account.impl;

import com.loan_org.identity_and_access_management.account.AccountStatusService;
import com.loan_org.identity_and_access_management.exception.AccountLockedException;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.user.entity.UserStatus;
import com.loan_org.identity_and_access_management.user.repository.UserRepository;
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