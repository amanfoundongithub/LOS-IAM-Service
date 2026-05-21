package com.loan_org.identity_and_access_management.dao;

import com.loan_org.identity_and_access_management.entity.PasswordResetTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface PasswordResetTokenDao extends MongoRepository<PasswordResetTokenDocument, String> {
    Optional<PasswordResetTokenDocument> findByToken(String token);
}
