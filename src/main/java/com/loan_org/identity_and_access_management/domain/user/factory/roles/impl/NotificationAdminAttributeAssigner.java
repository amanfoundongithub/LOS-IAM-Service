package com.loan_org.identity_and_access_management.domain.user.factory.roles.impl;

import com.loan_org.identity_and_access_management.domain.user.entity.UserRole;
import com.loan_org.identity_and_access_management.domain.user.factory.permissions.NotificationTemplatePermissions;
import com.loan_org.identity_and_access_management.domain.user.factory.roles.UserRoleAttributeAssignerAbstract;
import org.springframework.stereotype.Component;

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
