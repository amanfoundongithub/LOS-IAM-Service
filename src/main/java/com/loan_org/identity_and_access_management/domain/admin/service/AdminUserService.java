package com.loan_org.identity_and_access_management.domain.admin.service;

import com.loan_org.identity_and_access_management.domain.admin.dto.UserAccountLockRequest;
import com.loan_org.identity_and_access_management.domain.admin.dto.UserAccountUnlockRequest;
import com.loan_org.identity_and_access_management.domain.admin.dto.UserSearchAttributes;
import com.loan_org.identity_and_access_management.domain.admin.dto.UserSearchResults;

public interface AdminUserService {
    UserSearchResults searchUsers(UserSearchAttributes searchAttributes);
    String            lockUser(String userId, String lockerId, UserAccountLockRequest request);
    String            unlockUser(String userId, String lockerId, UserAccountUnlockRequest request);
}
