package com.loan_org.identity_and_access_management.admin.service;

import com.loan_org.identity_and_access_management.admin.model.user_search.UserSearchAttributes;
import com.loan_org.identity_and_access_management.admin.model.user_search.UserSearchResults;

public interface AdminUserSearchService {

    /**
     * 
     * Search for users based on the search attributes
     * @param searchAttributes The search attribute(s) for searching user(s)
     * @return
     */
    UserSearchResults searchUsers(UserSearchAttributes searchAttributes);
}
