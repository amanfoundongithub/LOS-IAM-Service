package com.loan_org.identity_and_access_management.dao;

import com.loan_org.identity_and_access_management.entity.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Data Access object for User details from MongoDB
 *
 * @author Aman Raj
 */
@Repository
public interface UserDao extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmail(String email);
    Optional<UserDocument> findByUsername(String username);
}