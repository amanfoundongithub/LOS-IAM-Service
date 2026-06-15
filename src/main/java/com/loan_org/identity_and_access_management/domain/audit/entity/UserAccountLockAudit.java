package com.loan_org.identity_and_access_management.domain.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_account_lock_audit")
public class UserAccountLockAudit {

    @Id
    private String id;

    private String lockedBy;
    private String lockedAccount;

    private String lockReason;

    @Builder.Default
    private String unlockReason = "Not applicable";

    @Builder.Default
    private boolean isStillLocked = true;

    private Instant lockedAt;

    private Instant unlockedAt;

}
