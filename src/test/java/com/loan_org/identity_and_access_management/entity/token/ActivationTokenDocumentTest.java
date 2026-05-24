package com.loan_org.identity_and_access_management.entity.token;

import com.loan_org.identity_and_access_management.entity.ActivationTokenDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.within;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ActivationTokenDocumentTest {

    @Test
    void testGettersSettersAndNoArgsConstructor() {
        // Arrange & Act (Testing No-Args Constructor & Setters)
        ActivationTokenDocument doc = new ActivationTokenDocument();
        Instant expiry = Instant.now().plus(24, ChronoUnit.HOURS);

        doc.setId("act-123");
        doc.setToken("activation-token-uuid");
        doc.setUserEmail("verify@loan_org.com");
        doc.setExpiresAt(expiry);

        // Assert (Testing Getters)
        assertThat(doc.getId()).isEqualTo("act-123");
        assertThat(doc.getToken()).isEqualTo("activation-token-uuid");
        assertThat(doc.getUserEmail()).isEqualTo("verify@loan_org.com");
        assertThat(doc.getExpiresAt()).isEqualTo(expiry);
    }

    @Test
    void testBuilderAndAllArgsConstructor() {
        Instant expiry = Instant.now().plus(2, ChronoUnit.HOURS);

        // Act (Testing Builder and All-Args Constructor paths)
        ActivationTokenDocument doc = ActivationTokenDocument.builder()
                .id("act-999")
                .token("builder-token")
                .userEmail("builder@loan_org.com")
                .expiresAt(expiry)
                .build();

        // Assert
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isEqualTo("act-999");
    }

    @Test
    void testCustomConstructorCalculatesCorrectExpiryInHours() {
        String sampleToken = "custom-act-token";
        String sampleEmail = "verify-custom@loan_org.com";
        int expiryHours = 2;

        // Act (Testing custom hourly business constructor)
        ActivationTokenDocument doc = new ActivationTokenDocument(sampleToken, sampleEmail, expiryHours);

        // Assert
        assertThat(doc.getToken()).isEqualTo(sampleToken);
        assertThat(doc.getUserEmail()).isEqualTo(sampleEmail);
        assertThat(doc.getId()).isNull();

        // Match with a 2-second tolerance threshold for runtime execution variance
        Instant expectedExpiry = Instant.now().plus(expiryHours, ChronoUnit.HOURS);
        assertThat(doc.getExpiresAt()).isCloseTo(expectedExpiry, within(2, ChronoUnit.SECONDS));
    }

    @Test
    void testIsExpiredReturnsFalseWhenTokenIsActive() {
        // Arrange: Token expiring far in the future
        ActivationTokenDocument doc = new ActivationTokenDocument();
        doc.setExpiresAt(Instant.now().plus(30, ChronoUnit.MINUTES));

        // Act & Assert (Branch 1: Active)
        assertThat(doc.isExpired()).isFalse();
    }

    @Test
    void testIsExpiredReturnsTrueWhenTokenHasPassed() {
        // Arrange: Token expired in the past
        ActivationTokenDocument doc = new ActivationTokenDocument();
        doc.setExpiresAt(Instant.now().minus(5, ChronoUnit.MINUTES));

        // Act & Assert (Branch 2: Expired)
        assertThat(doc.isExpired()).isTrue();
    }
}
