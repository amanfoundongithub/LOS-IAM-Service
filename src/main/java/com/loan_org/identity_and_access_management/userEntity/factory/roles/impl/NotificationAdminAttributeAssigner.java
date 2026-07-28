package com.loan_org.identity_and_access_management.userEntity.factory.roles.impl;

import org.springframework.stereotype.Component;

import com.loan_org.identity_and_access_management.userEntity.entity.UserRole;
import com.loan_org.identity_and_access_management.userEntity.factory.permissions.NotificationTemplatePermissions;
import com.loan_org.identity_and_access_management.userEntity.factory.roles.UserRoleAttributeAssignerAbstract;

@Component
public class NotificationAdminAttributeAssigner extends UserRoleAttributeAssignerAbstract {

    protected NotificationAdminAttributeAssigner() {
        super(UserRole.NOTIFICATION_SERVICE_ADMIN);
    }

    @Override
    protected void addPermissions() {
        this.userAttributes.put(NotificationTemplatePermissions.NOTIFICATION_TEMPLATE_CREATE, "true");
        this.userAttributes.put(NotificationTemplatePermissions.NOTIFICATION_TEMPLATE_UPDATE, "true");
        this.userAttributes.put(NotificationTemplatePermissions.NOTIFICATION_TEMPLATE_DELETE, "true");
        this.userAttributes.put(NotificationTemplatePermissions.NOTIFICATION_TEMPLATE_READ, "true");
    }

}
