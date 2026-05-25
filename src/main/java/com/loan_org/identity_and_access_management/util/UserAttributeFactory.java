package com.loan_org.identity_and_access_management.util;

import com.loan_org.identity_and_access_management.dto.UserRegistrationDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class UserAttributeFactory {

    public static final String KEY_MAX_APPROVAL_LIMIT = "max_approval_limit_inr";
    public static final String KEY_USER_ROLE = "user_role";

    public Map<String, Object> buildRegistrationAttributes(UserRegistrationDto registrationData) {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(KEY_MAX_APPROVAL_LIMIT, registrationData.getSigningLimit());
        attributes.put(KEY_USER_ROLE, registrationData.getRole());

        return attributes;
    }
}
