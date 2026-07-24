package com.loan_org.identity_and_access_management.audit.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.loan_org.identity_and_access_management.audit.entity.UserAccountModificationAuditDocument;
import com.loan_org.identity_and_access_management.audit.repository.UserAccountModificationAuditDocumentRepository;
import com.loan_org.identity_and_access_management.audit.service.UserAccountModificationAuditService;

import java.util.ArrayList;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserAccountModificationAuditServiceImp implements UserAccountModificationAuditService {

    private final UserAccountModificationAuditDocumentRepository userAccountModificationAuditDocumentRepository;

    private UserAccountModificationAuditDocument getInstance(String affectedUser) {
        Optional<UserAccountModificationAuditDocument> document = userAccountModificationAuditDocumentRepository.findByAffectedUser(affectedUser);
        if(document.isEmpty()) {
            log.info("No audit record found for affected user {}. Creating one...",
                    affectedUser);
            UserAccountModificationAuditDocument userAccountModificationAuditDocument
                    = UserAccountModificationAuditDocument.builder()
                    .affectedUser(affectedUser).modificationList(new ArrayList<>()).build();
            userAccountModificationAuditDocumentRepository.save(userAccountModificationAuditDocument);
            return userAccountModificationAuditDocument;
        }
        return document.get();
    }

    @Override
    public void addLog(String affectedUser, UserAccountModificationAuditDocument.ModificationListEntity logEntry) {
        UserAccountModificationAuditDocument document = getInstance(affectedUser);
        document.getModificationList().add(logEntry);
        userAccountModificationAuditDocumentRepository.save(document);
    }


}
