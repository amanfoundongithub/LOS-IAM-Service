package com.loan_org.identity_and_access_management.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Data
@Document(collection = "password_reset_token")
public class PasswordResetTokenDocument {

    @Id
    private String id;
    private String token;
    private String userEmail;
    private Instant expiryDate;

    public PasswordResetTokenDocument(String token, String userEmail, int expiryHours) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiryDate = Instant.now().plus(expiryHours, ChronoUnit.HOURS);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }
}
