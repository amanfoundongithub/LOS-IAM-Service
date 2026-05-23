package com.loan_org.identity_and_access_management.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Document(collection = "activation_token")
@Data
@NoArgsConstructor
public class ActivationTokenDocument {

    @Id
    private String id;
    private String token;
    private String userEmail;
    private Instant expiryDate;

    public ActivationTokenDocument(String token, String userEmail, int expiryHours) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiryDate = Instant.now().plus(expiryHours, ChronoUnit.HOURS);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(this.expiryDate);
    }

}
