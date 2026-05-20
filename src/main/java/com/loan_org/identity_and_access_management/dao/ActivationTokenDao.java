package com.loan_org.identity_and_access_management.dao;

import com.loan_org.identity_and_access_management.entity.ActivationTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

public interface ActivationTokenDao extends MongoRepository<ActivationTokenDocument, String> {
    Optional<ActivationTokenDocument> findByToken(String token);
}
