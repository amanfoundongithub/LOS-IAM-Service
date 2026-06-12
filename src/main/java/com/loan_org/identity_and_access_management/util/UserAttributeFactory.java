package com.loan_org.identity_and_access_management.util;

import com.loan_org.identity_and_access_management.dto.UserRegistrationDto;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class UserAttributeFactory {

    public static final String KEY_MAX_APPROVAL_LIMIT = "max_approval_limit_inr";
    public static final String KEY_USER_ROLE = "user_role";

    public static final String DOCUMENT_UPLOAD_PERMISSION = "document:upload";
    public static final String DOCUMENT_DOWNLOAD_PERMISSION = "document:download";
    public static final String DOCUMENT_FETCH_PERMISSION = "document:view";
    public static final String DOCUMENT_DELETE_PERMISSION = "document:delete";

    public Map<String, Object> buildRegistrationAttributes(UserRegistrationDto registrationData) {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(KEY_MAX_APPROVAL_LIMIT, registrationData.getSigningLimit());
        attributes.put(KEY_USER_ROLE, registrationData.getRole());

        if(Objects.equals(registrationData.getRole(), "LOAN_OFFICER")) {
            getAttributesForLoanOfficer(attributes);
        }

        return attributes;
    }

    private void getAttributesForLoanOfficer(Map<String, Object> attributes) {
        attributes.put(DOCUMENT_UPLOAD_PERMISSION, true);
        attributes.put(DOCUMENT_DOWNLOAD_PERMISSION, true);
        attributes.put(DOCUMENT_FETCH_PERMISSION, true);
        attributes.put(DOCUMENT_DELETE_PERMISSION, true);
    }


}
