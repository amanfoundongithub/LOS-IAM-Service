package com.loan_org.identity_and_access_management.token.refresh;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository interface for managing persistence operations related to
 * {@link RefreshTokenDocument} entities in MongoDB.
 *
 * <p>
 * Provides standard CRUD functionality through
 * {@link org.springframework.data.mongodb.repository.MongoRepository}
 * along with custom query and deletion operations for refresh token handling.
 * </p>
 *
 * <p>
 * This repository is used by the authentication subsystem to support
 * JWT refresh workflows, token rotation, session management,
 * and secure user re-authentication.
 * </p>
 *
 * <p>
 * Expected database constraints:
 * <ul>
 *     <li>Refresh token values must be unique</li>
 *     <li>Expired tokens should be periodically cleaned up</li>
 * </ul>
 * </p>
 *
 * @author Aman Raj
 * @since 1.0
 */
public interface RefreshTokenRepository extends MongoRepository<RefreshTokenDocument, String> {

    /**
     * Retrieves a refresh token document by its token value.
     *
     * @param token the refresh token string
     * @return an {@link Optional} containing the matching refresh token
     * document if found, otherwise an empty {@link Optional}
     */
    Optional<RefreshTokenDocument> findByToken(String token);

    /**
     * Deletes all refresh tokens associated with the specified user email.
     *
     * <p>
     * Typically used during logout, forced session invalidation,
     * password reset, or account security operations.
     * </p>
     *
     * @param userEmail the email address associated with the refresh tokens
     */
    void deleteByUserEmail(String userEmail);
}
