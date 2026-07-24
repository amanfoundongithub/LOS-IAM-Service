package com.loan_org.identity_and_access_management.admin.service.impl;

import java.time.Instant;

import org.springframework.stereotype.Service;

import com.loan_org.identity_and_access_management.admin.model.account_lock.UserAccountLockRequest;
import com.loan_org.identity_and_access_management.admin.model.account_unlock.UserAccountUnlockRequest;
import com.loan_org.identity_and_access_management.admin.service.AdminLockService;
import com.loan_org.identity_and_access_management.domain.audit.entity.UserAccountModificationAuditDocument;
import com.loan_org.identity_and_access_management.domain.audit.service.UserAccountModificationAuditService;
import com.loan_org.identity_and_access_management.domain.token.repository.RefreshTokenRepository;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.domain.user.entity.UserStatus;
import com.loan_org.identity_and_access_management.domain.user.repository.UserRepository;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminLockServiceImpl implements AdminLockService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccountModificationAuditService userAccountModificationAuditService;
    
    @Override
    public String lockUser(String lockerId, UserAccountLockRequest request) {

        log.info("Received request from ADMIN to lock a user account. Finding user in DB before locking...");
        String userId = request.userId();
        UserDocument userDocument = userRepository.findById(userId)
                .orElseThrow(() -> new AccountNotFoundException("No user found with id: " + userId));
        
        log.info("Found user in DB. Checking the account status...");
        UserStatus originalStatus = userDocument.getStatus();
        if(originalStatus == UserStatus.LOCKED){
            log.warn("The user account is already locked. Aborting...");
            return "User account is already locked";
        }
        userDocument.setStatus(UserStatus.LOCKED);
        userRepository.save(userDocument);
        
        log.info("Successfully locked user in DB. Removing all active session(s)...");
        refreshTokenRepository.deleteByUserEmail(userDocument.getEmail());

        log.info("All active sessions are removed. Locking SUCCESS!");
        UserAccountModificationAuditDocument.ModificationListEntity logEntry =
                UserAccountModificationAuditDocument.ModificationListEntity.builder()
                        .modificationDate(Instant.now())
                        .modificationDoneBy(lockerId)
                        .modifiedAttribute("status")
                        .originalValue(originalStatus)
                        .newValue(UserStatus.LOCKED)
                        .modificationReason(request.reason())
                        .build();
        userAccountModificationAuditService.addLog(userDocument.getEmail(), logEntry);
        return "User account has been locked successfully";

    }

    @Override
    public String unlockUser(String unlockerId, UserAccountUnlockRequest request) {

        log.info("Received request from ADMIN to unlock a user account.");
        String userId = request.userId();
        UserDocument userDocument = userRepository.findById(userId)
                .orElseThrow(() -> new AccountNotFoundException("No user found with id to unlock: " + userId));
        UserStatus originalStatus = userDocument.getStatus();
        if(originalStatus != UserStatus.LOCKED){
            log.info("User account is already unlocked.");
            return "User account is already unlocked. No need to unlock again.";
        }

        log.info("Found locked user in DB. Starting unlocking...");
        userDocument.setStatus(UserStatus.ACTIVE);
        userRepository.save(userDocument);

        log.info("Successfully unlocked user in DB. Unlocking SUCCESS");
        UserAccountModificationAuditDocument.ModificationListEntity logEntry =
                UserAccountModificationAuditDocument.ModificationListEntity.builder()
                        .modificationDate(Instant.now())
                        .modificationDoneBy(unlockerId)
                        .modifiedAttribute("status")
                        .originalValue(originalStatus)
                        .newValue(UserStatus.ACTIVE)
                        .modificationReason(request.reason())
                        .build();
        userAccountModificationAuditService.addLog(userDocument.getEmail(), logEntry);
        return "User account has been unlocked successfully";
        
    }

    
    
}
