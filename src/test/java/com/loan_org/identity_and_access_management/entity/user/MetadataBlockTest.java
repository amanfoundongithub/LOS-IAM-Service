package com.loan_org.identity_and_access_management.entity.user;

import com.loan_org.identity_and_access_management.domain.user.entity.MetadataBlock;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class MetadataBlockTest {

    @Test
    void testGettersSettersAndNoArgsConstructor() {
        // Arrange & Act (Testing No-Args Constructor & Setters)
        MetadataBlock metadataBlock = new MetadataBlock();
        Instant now = Instant.now();

        metadataBlock.setCreatedAt(now);
        metadataBlock.setUpdatedAt(now);
        metadataBlock.setLastLoginAt(now);

        // Assert (Testing Getters)
        assertThat(metadataBlock.getCreatedAt()).isEqualTo(now);
        assertThat(metadataBlock.getUpdatedAt()).isEqualTo(now);
        assertThat(metadataBlock.getLastLoginAt()).isEqualTo(now);
    }

    @Test
    void testAllArgsConstructorAndBuilder() {
        Instant now = Instant.now();

        // Act (Testing Builder and All-Args Constructor)
        MetadataBlock metadataBlock = MetadataBlock.builder()
                .createdAt(now)
                .updatedAt(now)
                .lastLoginAt(now)
                .build();

        // Assert
        assertThat(metadataBlock).isNotNull();
        assertThat(metadataBlock.getCreatedAt()).isEqualTo(now);
        assertThat(metadataBlock.getUpdatedAt()).isEqualTo(now);
        assertThat(metadataBlock.getLastLoginAt()).isEqualTo(now);
    }

}
