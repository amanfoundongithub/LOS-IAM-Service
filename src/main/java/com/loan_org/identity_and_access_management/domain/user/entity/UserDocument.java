package com.loan_org.identity_and_access_management.domain.user.entity;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.Map;

/**
 * Root MongoDB document representing a user account in the
 * Identity and Access Management (IAM) system.
 *
 * <p>Encapsulates identity data, security information,
 * access-related attributes, and metadata.</p>
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

    @Indexed(unique = true)
    @NotBlank
    @Email
    private String email;

    @Indexed(unique = true)
    @NotBlank
    private String username;

    // Status of user's account
    private UserStatus status;

    // Security block stores secured data
    private SecurityBlock security;

    // Access management block
    private Map<String, Object> attributes;

    @Builder.Default
    private MetadataBlock metadata = new MetadataBlock();

}
