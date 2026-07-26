package com.loan_org.identity_and_access_management.userEntity.factory.roles.impl;

import org.springframework.stereotype.Component;

import com.loan_org.identity_and_access_management.userEntity.entity.UserRole;
import com.loan_org.identity_and_access_management.userEntity.factory.permissions.DocumentPermissions;
import com.loan_org.identity_and_access_management.userEntity.factory.roles.UserRoleAttributeAssignerAbstract;

@Component
public class RelationshipManagerAttributeAssigner extends UserRoleAttributeAssignerAbstract {

    public RelationshipManagerAttributeAssigner() {
        super(UserRole.RELATIONSHIP_MANAGER);
    }

    @Override
    protected void addPermissions() {
        this.userAttributes.put(DocumentPermissions.DOCUMENT_UPLOAD, "true");
        this.userAttributes.put(DocumentPermissions.DOCUMENT_DOWNLOAD, "true");
        this.userAttributes.put(DocumentPermissions.DOCUMENT_UPDATE, "true");
    }

}
