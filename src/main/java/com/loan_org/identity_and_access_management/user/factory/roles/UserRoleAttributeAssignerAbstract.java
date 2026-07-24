package com.loan_org.identity_and_access_management.user.factory.roles;

import java.util.HashMap;
import java.util.Map;

import com.loan_org.identity_and_access_management.user.entity.UserRole;

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
