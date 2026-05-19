package com.loan_org.identity_and_access_management.dao;

import com.loan_org.identity_and_access_management.entity.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

/**
 * Data Access object for User details from MongoDB
 *
 * @author Aman Raj
 */
public interface UserDao extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmail(String email);
    Optional<UserDocument> findByUsername(String username);
}