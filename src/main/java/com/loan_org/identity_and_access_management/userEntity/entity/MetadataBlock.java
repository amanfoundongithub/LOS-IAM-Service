package com.loan_org.identity_and_access_management.userEntity.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * Embedded operational metadata model associated with a user account.
 *
 * Stores lifecycle timestamps and activity-related information
 * used for auditing and operational tracking.
 *
 * @author Aman Raj
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetadataBlock {

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    /**
     * Last successful authentication.
     */
    private Instant lastLoginAt;

    /**
     * Last observed activity.
     */
    private Instant lastSeenAt;

    /**
     * Creator of this account.
     * Typically SYSTEM or an administrator email.
     */
    private String createdBy;

    /**
     * Last user/admin who modified this account.
     */
    private String updatedBy;
}