package com.loan_org.identity_and_access_management.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

/**
 * MongoDB instance of the entire user entity. Contains all fields
 * related to security and identity management.
 *
 * @author Aman Raj
 */
@Data
@Document(collection = "user")
public class UserDocument {

    @Id
    private String id;
    private String email;
    private String username;
    private UserStatus status;

    // Security block stores secured data
    private SecurityBlock security;

    // Access management block
    private Map<String, Object> attributes;

    // Metadata block
    private MetadataBlock metadata;

}
