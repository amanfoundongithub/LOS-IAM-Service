package com.loan_org.identity_and_access_management.domain.admin.service.impl;

import com.loan_org.identity_and_access_management.domain.admin.dto.UserAccountLockRequest;
import com.loan_org.identity_and_access_management.domain.admin.dto.UserAccountUnlockRequest;
import com.loan_org.identity_and_access_management.domain.admin.dto.UserSearchAttributes;
import com.loan_org.identity_and_access_management.domain.admin.dto.UserSearchResults;
import com.loan_org.identity_and_access_management.domain.admin.service.AdminUserService;
import com.loan_org.identity_and_access_management.domain.audit.entity.UserAccountLockAudit;
import com.loan_org.identity_and_access_management.domain.audit.repository.UserAccountLockAuditRepository;
import com.loan_org.identity_and_access_management.domain.token.repository.RefreshTokenRepository;
import com.loan_org.identity_and_access_management.domain.user.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.domain.user.entity.UserStatus;
import com.loan_org.identity_and_access_management.domain.user.repository.UserRepository;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserAccountLockAuditRepository userAccountLockAuditRepository;

    @Override
    public UserSearchResults searchUsers(UserSearchAttributes searchAttributes) {

        log.info("Received ADMIN request to search for users based on criteria: {}",
                searchAttributes.toString());

        // Sort criteria
        String sortBy = searchAttributes.sortBy();
        String sortDir = searchAttributes.sortDir();
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Pageable result
        Pageable pageable = PageRequest.of(searchAttributes.page(), searchAttributes.size(), sort);

        // Sample reference
        UserDocument sampleDocument = new UserDocument();
        if (searchAttributes.role() != null) {
            sampleDocument.getAttributes().put("role", searchAttributes.role());
        }
        if (searchAttributes.status() != null){
            sampleDocument.setStatus(UserStatus.valueOf(searchAttributes.status()));
        }

        // Matcher
        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreNullValues();
        Example<UserDocument> example = Example.of(sampleDocument, matcher);

        // Find all documents in the page
        Page<UserDocument> userPage = userRepository.findAll(example, pageable);
        log.info("Found {} users based on criteria: {}",
                userPage.getTotalElements(),
                userPage.toString());

        List<UserResponseDto> dtoList = userPage.getContent().stream()
                .map(this::mapToResponseDto)
                .toList();
        log.info("Filtered & found {} users based on the example: {}",
                dtoList.size(),
                example.toString());

        return UserSearchResults.builder()
                .content(dtoList)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .isLast(userPage.isLast())
                .build();
    }

    @Override
    public String lockUser(String userId, String lockerEmail, UserAccountLockRequest request) {
        log.info("Received request from ADMIN to lock a user account.");
        UserDocument userDocument = userRepository.findById(userId)
                .orElseThrow(() -> new AccountNotFoundException("No user found with id: " + userId));
        if(userDocument.getStatus() == UserStatus.LOCKED){
            log.info("User account is already locked by another admin.");
            return "User account is already locked. No need to lock again.";
        }

        log.info("Found the unlocked user in DB. Starting locking...");
        userDocument.setStatus(UserStatus.LOCKED);
        userRepository.save(userDocument);

        log.info("Locked user in DB. Removing all active session(s)...");
        refreshTokenRepository.deleteByUserEmail(userDocument.getEmail());

        log.info("All sessions deleted. Locking SUCCESS.");
        UserAccountLockAudit audit = UserAccountLockAudit.builder()
                .lockedBy(lockerEmail)
                .lockedAccount(userDocument.getEmail())
                .lockReason(request.reason())
                .lockedAt(Instant.now())
                .build();
        userAccountLockAuditRepository.save(audit);
        return "User account has been locked successfully";
    }

    @Override
    public String unlockUser(String userId, String lockerEmail, UserAccountUnlockRequest request) {
        log.info("Received request from ADMIN to unlock a user account.");
        UserDocument userDocument = userRepository.findById(userId)
                .orElseThrow(() -> new AccountNotFoundException("No user found with id to unlock: " + userId));
        if(userDocument.getStatus() != UserStatus.LOCKED){
            log.info("User account is already unlocked.");
            return "User account is already unlocked. No need to unlock again.";
        }

        log.info("Found locked user in DB. Starting unlocking...");
        userDocument.setStatus(UserStatus.ACTIVE);
        userRepository.save(userDocument);

        UserAccountLockAudit audit = userAccountLockAuditRepository.findByLockedByAndLockedAccount(
                lockerEmail, userDocument.getEmail()
        ).orElseThrow(() -> new AccountNotFoundException("No locker found with id to unlock: " + userId));

        log.info("Successfully unlocked user in DB! UNLOCKING SUCCESS.");

        audit.setStillLocked(false);
        audit.setUnlockReason(request.reason());
        audit.setUnlockedAt(Instant.now());
        userAccountLockAuditRepository.save(audit);
        return "User account has been unlocked successfully";
    }


    private UserResponseDto mapToResponseDto(UserDocument userDocument) {
        return UserResponseDto.builder()
                .id(userDocument.getId())
                .email(userDocument.getEmail())
                .username(userDocument.getUsername())
                .status(userDocument.getStatus())
                .attributes(userDocument.getAttributes())
                .build();
    }
}
