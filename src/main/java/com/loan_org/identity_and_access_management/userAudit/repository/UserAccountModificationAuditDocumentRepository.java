package com.loan_org.identity_and_access_management.userAudit.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.loan_org.identity_and_access_management.userAudit.entity.UserAccountModificationAuditDocument;

import java.util.Optional;

public interface UserAccountModificationAuditDocumentRepository extends MongoRepository<UserAccountModificationAuditDocument, String> {

    /**
     * Find the modification list for the affected user
     *
     * @param affectedUser Email of the affected user
     * @return The document
     */
    Optional<UserAccountModificationAuditDocument> findByAffectedUser(String affectedUser);
}
