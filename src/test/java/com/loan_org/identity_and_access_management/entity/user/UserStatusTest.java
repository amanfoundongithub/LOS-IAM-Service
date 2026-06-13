package com.loan_org.identity_and_access_management.entity.user;

import com.loan_org.identity_and_access_management.domain.user.entity.UserStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class UserStatusTest {

    @Test
    void testEnumValuesAndMapping() {
        // 1. Verify the exact number of enum constants exist
        UserStatus[] statuses = UserStatus.values();
        assertThat(statuses).hasSize(4);

        // 2. Test valueOf() to verify valid string lookups (Covers internal branching)
        assertThat(UserStatus.valueOf("PENDING_VERIFICATION")).isEqualTo(UserStatus.PENDING_VERIFICATION);
        assertThat(UserStatus.valueOf("ACTIVE")).isEqualTo(UserStatus.ACTIVE);
        assertThat(UserStatus.valueOf("SUSPENDED")).isEqualTo(UserStatus.SUSPENDED);
        assertThat(UserStatus.valueOf("ARCHIVED")).isEqualTo(UserStatus.ARCHIVED);
    }
}
