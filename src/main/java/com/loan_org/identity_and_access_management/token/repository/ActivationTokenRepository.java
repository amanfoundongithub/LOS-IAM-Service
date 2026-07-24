package com.loan_org.identity_and_access_management.token.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.loan_org.identity_and_access_management.token.entity.ActivationTokenDocument;

import java.util.Optional;

/**
 * Repository interface for managing persistence operations related to
 * {@link ActivationTokenDocument} entities in MongoDB.
 *
 * <p>
 * Provides standard CRUD operations through
 * {@link org.springframework.data.mongodb.repository.MongoRepository}
 * along with custom lookup functionality for account activation tokens.
 * </p>
 *
 * <p>
 * This repository is used by the account registration and verification
 * subsystem to support email verification workflows, account activation,
 * and temporary activation token management.
 * </p>
 *
 * <p>
 * Expected database constraints:
 * <ul>
 *     <li>Activation tokens must be unique</li>
 *     <li>Tokens should have a defined expiration time</li>
 *     <li>Expired or consumed tokens should be periodically removed</li>
 * </ul>
 * </p>
 *
 * @author Aman Raj
 * @since 1.0
 */
public interface ActivationTokenRepository
        extends MongoRepository<ActivationTokenDocument, String> {

    /**
     * Retrieves an activation token document by its token value.
     *
     * @param token the account activation token string
     * @return an {@link Optional} containing the matching activation token
     * document if found, otherwise an empty {@link Optional}
     */
    Optional<ActivationTokenDocument> findByToken(String token);
}