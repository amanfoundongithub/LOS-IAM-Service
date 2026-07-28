package com.loan_org.identity_and_access_management.userAudit.service;

import com.loan_org.identity_and_access_management.userAudit.entity.UserAccountModificationAuditDocument;

public interface UserAccountModificationAuditService {
    void addLog(String affectedUser,UserAccountModificationAuditDocument.ModificationListEntity logEntry);
}
