package com.loan_org.identity_and_access_management.domain.audit.repository;

import com.loan_org.identity_and_access_management.domain.audit.entity.UserAccountModificationAuditDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserAccountModificationAuditDocumentRepository extends MongoRepository<UserAccountModificationAuditDocument, String> {

    /**
     * Find the modification list for the affected user
     *
     * @param affectedUser Email of the affected user
     * @return The document
     */
    Optional<UserAccountModificationAuditDocument> findByAffectedUser(String affectedUser);
}
