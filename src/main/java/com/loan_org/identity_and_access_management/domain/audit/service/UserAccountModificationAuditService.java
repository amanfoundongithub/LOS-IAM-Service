package com.loan_org.identity_and_access_management.domain.audit.service;

import com.loan_org.identity_and_access_management.domain.audit.entity.UserAccountModificationAuditDocument;

public interface UserAccountModificationAuditService {
    void addLog(String affectedUser,UserAccountModificationAuditDocument.ModificationListEntity logEntry);
}
