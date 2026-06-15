package com.loan_org.identity_and_access_management.domain.audit.repository;

import com.loan_org.identity_and_access_management.domain.audit.entity.UserAccountLockAudit;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountLockAuditRepository extends MongoRepository<UserAccountLockAudit, String> {
    Optional<UserAccountLockAudit> findByLockedByAndLockedAccount(String lockedBy, String lockedAccount);
}
