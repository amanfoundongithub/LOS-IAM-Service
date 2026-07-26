package com.loan_org.identity_and_access_management.user.repository;

import com.loan_org.identity_and_access_management.user.entity.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Repository interface for performing persistence operations on
 * {@link UserDocument} entities in MongoDB.
 *
 * <p>
 * Provides standard CRUD operations through
 * {@link MongoRepository} along with common lookup methods
 * required by the Identity and Access Management (IAM) service.
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
public interface UserRepository extends MongoRepository<UserDocument, String> {

    /**
     * Finds a user by their unique email address.
     *
     * @param email user's email
     * @return matching user if present
     */
    Optional<UserDocument> findByEmail(String email);

    /**
     * Finds a user by their unique username.
     *
     * @param username user's username
     * @return matching user if present
     */
    Optional<UserDocument> findByUsername(String username);

    /**
     * Finds a user by either email or username.
     * Useful for login flows where either identifier is accepted.
     *
     * @param email email to search
     * @param username username to search
     * @return matching user if present
     */
    Optional<UserDocument> findByEmailOrUsername(String email, String username);

    /**
     * Checks whether an email is already registered.
     *
     * @param email user's email
     * @return true if email already exists
     */
    boolean existsByEmail(String email);

    /**
     * Checks whether a username is already registered.
     *
     * @param username user's username
     * @return true if username already exists
     */
    boolean existsByUsername(String username);
}