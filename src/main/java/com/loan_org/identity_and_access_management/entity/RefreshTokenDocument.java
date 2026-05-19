package com.loan_org.identity_and_access_management.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.Indexed;

import java.time.Instant;

@Data
@Document(collection = "refresh_token")
public class RefreshTokenDocument {
    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    private String userEmail;
    private Instant expiryDate;

    @Indexed(name = "expire_at_idx", expireAfterSeconds = 0)
    private Instant expireAt;

}
