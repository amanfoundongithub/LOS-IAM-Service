package com.loan_org.identity_and_access_management.entity;

import lombok.Data;
import java.time.Instant;

/**
 * Metadata block stores the information related to user's metadata
 * for enhanced experience.
 *
 * @author Aman Raj
 */
@Data
public class MetadataBlock {
    private Instant createdAt;
    private Instant updatedAt;
    private Instant lastLoginAt;
}