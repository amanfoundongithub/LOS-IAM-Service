package com.loan_org.identity_and_access_management.admin.service;

import com.loan_org.identity_and_access_management.admin.model.account_lock.UserAccountLockRequest;
import com.loan_org.identity_and_access_management.admin.model.account_unlock.UserAccountUnlockRequest;

public interface AdminLockService {

    /**
     * Locks the user's account
     * 
     * @param lockerId The admin responsible for lock
     * @param request  The supporting request
     * @return The status of lock
     */
    String lockUser(String lockerId, UserAccountLockRequest request);

    /**
     * Unlock the user's account
     * 
     * @param lockerId The admin responsible for unlock
     * @param request  The supporting request
     * @return The status of unlock
     */
    String unlockUser(String unlockerId, UserAccountUnlockRequest request);

}
