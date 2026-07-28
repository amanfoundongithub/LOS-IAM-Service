package com.loan_org.identity_and_access_management.userEntity.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * Root MongoDB document representing a user account in the
 * Identity and Access Management (IAM) system.
 *
 * Encapsulates identity data, security information,
 * access-related attributes, and metadata.
 *
 * @author Aman Raj
 */
@Document(collection = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDocument {

    @Id
    @Setter(AccessLevel.NONE)
    private String id;

    @Version
    private Long version;

    @Indexed(unique = true)
    @NotBlank
    @Email
    private String email;

    @Indexed(unique = true)
    @NotBlank
    private String username;

    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    @Builder.Default
    private SecurityBlock security = new SecurityBlock();

    /**
     * Access-related user attributes.
     * Examples:
     * user_role
     * department
     * branch
     * permissions
     */
    private Map<String, Object> attributes;

    @Builder.Default
    private MetadataBlock metadata = new MetadataBlock();
}