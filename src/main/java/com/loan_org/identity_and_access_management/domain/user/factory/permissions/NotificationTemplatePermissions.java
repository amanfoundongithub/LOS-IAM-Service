package com.loan_org.identity_and_access_management.domain.user.factory.permissions;

public class NotificationTemplatePermissions {

    public static final String NOTIFICATION_TEMPLATE_CREATE = "notificationTemplate:create";
    public static final String NOTIFICATION_TEMPLATE_UPDATE = "notificationTemplate:update";
    public static final String NOTIFICATION_TEMPLATE_DELETE = "notificationTemplate:delete";
    public static final String NOTIFICATION_TEMPLATE_READ = "notificationTemplate:read";

    private NotificationTemplatePermissions() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

}
