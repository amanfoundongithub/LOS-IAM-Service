package com.loan_org.identity_and_access_management.entity.token;

import com.loan_org.identity_and_access_management.entity.PasswordResetTokenDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class PasswordResetTokenDocumentTest {

    @Test
    void testGettersSettersAndNoArgsConstructor() {
        // Arrange & Act (Testing No-Args Constructor & Setters)
        PasswordResetTokenDocument doc = new PasswordResetTokenDocument();
        Instant expiry = Instant.now().plus(1, ChronoUnit.HOURS);

        doc.setId("reset-id-123");
        doc.setToken("reset-token-uuid");
        doc.setUserEmail("recovery@loan_org.com");
        doc.setExpiresAt(expiry);

        // Assert (Testing Getters)
        assertThat(doc.getId()).isEqualTo("reset-id-123");
        assertThat(doc.getToken()).isEqualTo("reset-token-uuid");
        assertThat(doc.getUserEmail()).isEqualTo("recovery@loan_org.com");
        assertThat(doc.getExpiresAt()).isEqualTo(expiry);
    }

    @Test
    void testBuilderAndAllArgsConstructor() {
        Instant expiry = Instant.now().plus(3, ChronoUnit.HOURS);

        // Act (Testing Builder and All-Args Constructor paths)
        PasswordResetTokenDocument doc = PasswordResetTokenDocument.builder()
                .id("reset-id-999")
                .token("builder-reset-token")
                .userEmail("builder@loan_org.com")
                .expiresAt(expiry)
                .build();

        // Assert
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isEqualTo("reset-id-999");
    }

    @Test
    void testCustomConstructorCalculatesCorrectExpiryInHours() {
        String sampleToken = "custom-reset-token";
        String sampleEmail = "reset-custom@loan_org.com";
        int expiryHours = 3;

        // Act (Testing custom business constructor)
        PasswordResetTokenDocument doc = new PasswordResetTokenDocument(sampleToken, sampleEmail, expiryHours);

        // Assert
        assertThat(doc.getToken()).isEqualTo(sampleToken);
        assertThat(doc.getUserEmail()).isEqualTo(sampleEmail);
        assertThat(doc.getId()).isNull();

        // Target an acceptable execution delta threshold (+/- 2 seconds)
        Instant expectedExpiry = Instant.now().plus(expiryHours, ChronoUnit.HOURS);
        assertThat(doc.getExpiresAt()).isCloseTo(expectedExpiry, within(2, ChronoUnit.SECONDS));
    }

    @Test
    void testIsExpiredReturnsFalseWhenTokenIsActive() {
        // Arrange: Expiry set to the future
        PasswordResetTokenDocument doc = new PasswordResetTokenDocument();
        doc.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));

        // Act & Assert (Branch 1: Active)
        assertThat(doc.isExpired()).isFalse();
    }

    @Test
    void testIsExpiredReturnsTrueWhenTokenHasPassed() {
        // Arrange: Expiry set to the past
        PasswordResetTokenDocument doc = new PasswordResetTokenDocument();
        doc.setExpiresAt(Instant.now().minus(15, ChronoUnit.MINUTES));

        // Act & Assert (Branch 2: Expired)
        assertThat(doc.isExpired()).isTrue();
    }
}
