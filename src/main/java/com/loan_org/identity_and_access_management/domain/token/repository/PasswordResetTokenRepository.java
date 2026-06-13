package com.loan_org.identity_and_access_management.domain.token.repository;

import com.loan_org.identity_and_access_management.domain.token.entity.PasswordResetTokenDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

/**
 * Repository interface for managing persistence operations related to
 * {@link PasswordResetTokenDocument} entities in MongoDB.
 *
 * <p>
 * Provides standard CRUD operations through
 * {@link org.springframework.data.mongodb.repository.MongoRepository}
 * along with custom lookup functionality for password reset tokens.
 * </p>
 *
 * <p>
 * This repository is used by the authentication and account recovery
 * subsystem to support secure password reset workflows, token validation,
 * and temporary recovery credential management.
 * </p>
 *
 * <p>
 * Expected database constraints:
 * <ul>
 *     <li>Password reset tokens must be unique</li>
 *     <li>Tokens should have a defined expiration time</li>
 *     <li>Expired or consumed tokens should be periodically removed</li>
 * </ul>
 * </p>
 *
 * @author Aman Raj
 * @since 1.0
 */
public interface PasswordResetTokenRepository
        extends MongoRepository<PasswordResetTokenDocument, String> {

    /**
     * Retrieves a password reset token document by its token value.
     *
     * @param token the password reset token string
     * @return an {@link Optional} containing the matching token document
     * if found, otherwise an empty {@link Optional}
     */
    Optional<PasswordResetTokenDocument> findByToken(String token);
}