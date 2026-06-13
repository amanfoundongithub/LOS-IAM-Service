package com.loan_org.identity_and_access_management.domain.user.repository;

import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for performing persistence operations on
 * {@link UserDocument} entities in MongoDB.
 *
 * <p>
 * Provides standard CRUD operations through
 * {@link org.springframework.data.mongodb.repository.MongoRepository}
 * along with custom query methods for user lookup by email and username.
 * </p>
 *
 * <p>
 * This repository is primarily used by the authentication and identity
 * management services for user retrieval, registration validation,
 * and credential-based authentication workflows.
 * </p>
 *
 * <p>
 * Expected database constraints:
 * <ul>
 *     <li>Email must be unique</li>
 *     <li>Username must be unique</li>
 * </ul>
 * </p>
 *
 * @author Aman Raj
 * @since 1.0
 */
@Repository
public interface UserRepository extends MongoRepository<UserDocument, String> {

    /**
     * Retrieves a user by their email address.
     *
     * @param email the unique email address of the user
     * @return an {@link Optional} containing the matching user if found,
     * otherwise an empty {@link Optional}
     */
    Optional<UserDocument> findByEmail(String email);

    /**
     * Retrieves a user by their username.
     *
     * @param username the unique username of the user
     * @return an {@link Optional} containing the matching user if found,
     * otherwise an empty {@link Optional}
     */
    Optional<UserDocument> findByUsername(String username);
}