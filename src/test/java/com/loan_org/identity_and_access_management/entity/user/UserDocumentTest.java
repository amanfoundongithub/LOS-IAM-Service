package com.loan_org.identity_and_access_management.entity.user;

import com.loan_org.identity_and_access_management.domain.user.entity.MetadataBlock;
import com.loan_org.identity_and_access_management.domain.user.entity.SecurityBlock;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.domain.user.entity.UserStatus;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserDocumentTest {

    @Test
    void testGettersSettersAndNoArgsConstructor() {
        // Arrange & Act (Testing No-Args Constructor & Setters)
        UserDocument user = new UserDocument();
        SecurityBlock security = new SecurityBlock();
        MetadataBlock metadata = new MetadataBlock();
        Map<String, Object> attributes = Collections.singletonMap("role", "ADMIN");

        user.setId("user-123");
        user.setEmail("test@loan_org.com");
        user.setUsername("testuser");
        user.setStatus(UserStatus.ACTIVE);
        user.setSecurity(security);
        user.setAttributes(attributes);
        user.setMetadata(metadata);

        // Assert (Testing Getters)
        assertThat(user.getId()).isEqualTo("user-123");
        assertThat(user.getEmail()).isEqualTo("test@loan_org.com");
        assertThat(user.getUsername()).isEqualTo("testuser");
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
        assertThat(user.getSecurity()).isEqualTo(security);
        assertThat(user.getAttributes()).isEqualTo(attributes);
        assertThat(user.getMetadata()).isEqualTo(metadata);
    }

    @Test
    void testBuilderWithAllFields() {
        SecurityBlock security = new SecurityBlock();
        MetadataBlock metadata = new MetadataBlock();
        Map<String, Object> attributes = Collections.singletonMap("tier", "PREMIUM");

        // Act (Testing Builder with everything explicitly provided)
        UserDocument user = UserDocument.builder()
                .id("user-999")
                .email("builder@loan_org.com")
                .username("builder_user")
                .status(UserStatus.PENDING_VERIFICATION)
                .security(security)
                .attributes(attributes)
                .metadata(metadata)
                .build();

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getId()).isEqualTo("user-999");
        assertThat(user.getMetadata()).isEqualTo(metadata);
    }

    @Test
    void testBuilderDefaultMetadataValue() {
        // Act (Testing Builder without providing metadata to trigger @Builder.Default code branch)
        UserDocument user = UserDocument.builder()
                .email("default@loan_org.com")
                .username("default_user")
                .build();

        // Assert
        assertThat(user).isNotNull();
        assertThat(user.getMetadata()).isNotNull(); // Verifies Lombok's default initialization branch was covered
    }

}
