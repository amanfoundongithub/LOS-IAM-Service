package com.loan_org.identity_and_access_management.domain.user.factory.roles;

import com.loan_org.identity_and_access_management.domain.user.entity.UserRole;

import java.util.HashMap;
import java.util.Map;

public abstract class UserRoleAttributeAssignerAbstract implements UserRoleAttributeAssigner {

    protected UserRole role;
    protected Map<String, Object> userAttributes;

    protected UserRoleAttributeAssignerAbstract(UserRole role) {
        this.role = role;
    }

    @Override
    public UserRole getRole() {
        return role;
    }

    @Override
    public Map<String, Object> assign() {

        // Attributes as hash map
        this.userAttributes = new HashMap<>();

        // Add user role here
        this.userAttributes.put("userRole", role.toString());

        // Add other permissions here
        this.addPermissions();

        // Return after all this
        return userAttributes;
    }

    protected abstract void addPermissions();
}
