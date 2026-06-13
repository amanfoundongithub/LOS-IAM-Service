package com.loan_org.identity_and_access_management.entity.token;

import com.loan_org.identity_and_access_management.domain.token.entity.RefreshTokenDocument;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class RefreshTokenDocumentTest {

    @Test
    void testGettersSettersAndNoArgsConstructor() {
        // Arrange & Act (Testing No-Args Constructor & Setters)
        RefreshTokenDocument doc = new RefreshTokenDocument();
        Instant expiry = Instant.now().plus(7, ChronoUnit.DAYS);

        doc.setId("token-id-123");
        doc.setToken("some-random-uuid-string");
        doc.setUserEmail("user@loan_org.com");
        doc.setExpiresAt(expiry);

        // Assert (Testing Getters)
        assertThat(doc.getId()).isEqualTo("token-id-123");
        assertThat(doc.getToken()).isEqualTo("some-random-uuid-string");
        assertThat(doc.getUserEmail()).isEqualTo("user@loan_org.com");
        assertThat(doc.getExpiresAt()).isEqualTo(expiry);
    }

    @Test
    void testBuilderAndAllArgsConstructor() {
        Instant expiry = Instant.now().plus(1, ChronoUnit.DAYS);

        // Act (Testing Builder and implicit All-Args Constructor)
        RefreshTokenDocument doc = RefreshTokenDocument.builder()
                .id("id-999")
                .token("jwt-refresh-token")
                .userEmail("builder@loan_org.com")
                .expiresAt(expiry)
                .build();

        // Assert
        assertThat(doc).isNotNull();
        assertThat(doc.getId()).isEqualTo("id-999");
    }

    @Test
    void testCustomConstructorCalculatesCorrectExpiry() {
        String sampleToken = "custom-constructor-token";
        String sampleEmail = "custom@loan_org.com";
        int expiryDays = 5;

        // Act (Testing the custom business constructor)
        RefreshTokenDocument doc = new RefreshTokenDocument(sampleToken, sampleEmail, expiryDays);

        // Assert
        assertThat(doc.getToken()).isEqualTo(sampleToken);
        assertThat(doc.getUserEmail()).isEqualTo(sampleEmail);
        assertThat(doc.getId()).isNull(); // ID shouldn't be set by this constructor

        // Allow a slight delta margin of 2 seconds for execution time variance
        Instant expectedExpiry = Instant.now().plus(expiryDays, ChronoUnit.DAYS);
// Assert that the expiration is within 2 seconds of what we expect
        assertThat(doc.getExpiresAt()).isCloseTo(expectedExpiry, org.assertj.core.api.Assertions.within(2, java.time.temporal.ChronoUnit.SECONDS));
    }

    @Test
    void testIsExpiredReturnsFalseWhenTokenIsActive() {
        // Arrange: Set expiry in the future
        RefreshTokenDocument doc = new RefreshTokenDocument();
        doc.setExpiresAt(Instant.now().plus(1, ChronoUnit.HOURS));

        // Act & Assert (Branch 1: Not expired)
        assertThat(doc.isExpired()).isFalse();
    }

    @Test
    void testIsExpiredReturnsTrueWhenTokenHasPassed() {
        // Arrange: Set expiry in the past
        RefreshTokenDocument doc = new RefreshTokenDocument();
        doc.setExpiresAt(Instant.now().minus(1, ChronoUnit.HOURS));

        // Act & Assert (Branch 2: Expired)
        assertThat(doc.isExpired()).isTrue();
    }

}
