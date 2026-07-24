package com.loan_org.identity_and_access_management.user.entity;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

/**
 * Embedded operational metadata model associated with a user account.
 *
 * <p>Stores lifecycle timestamps and activity-related information
 * used for auditing, account monitoring, and operational tracking.</p>
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

    private Instant lastLoginAt;
}