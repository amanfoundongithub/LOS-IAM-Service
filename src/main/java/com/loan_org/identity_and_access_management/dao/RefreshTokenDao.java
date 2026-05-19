package com.loan_org.identity_and_access_management.dao;

import com.loan_org.identity_and_access_management.entity.RefreshTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface RefreshTokenDao extends MongoRepository<RefreshTokenDocument, String> {
    Optional<RefreshTokenDocument> findByToken(String token);
    void deleteByUserEmail(String userEmail);
}
