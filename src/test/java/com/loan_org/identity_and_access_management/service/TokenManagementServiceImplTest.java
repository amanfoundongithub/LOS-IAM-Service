package com.loan_org.identity_and_access_management.service;

import com.loan_org.identity_and_access_management.dao.ActivationTokenDao;
import com.loan_org.identity_and_access_management.dao.PasswordResetTokenDao;
import com.loan_org.identity_and_access_management.dao.RefreshTokenDao;
import com.loan_org.identity_and_access_management.dao.UserDao;
import com.loan_org.identity_and_access_management.dto.RefreshTokenRequestDto;
import com.loan_org.identity_and_access_management.entity.*;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.exception.TokenNotProvidedException;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import com.loan_org.identity_and_access_management.security.JwtService;
import com.loan_org.identity_and_access_management.service.impl.TokenManagementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenManagementServiceImplTest {

    @Mock private UserDao userDao;
    @Mock private RefreshTokenDao refreshTokenDao;
    @Mock private ActivationTokenDao activationTokenDao;
    @Mock private PasswordResetTokenDao passwordResetTokenDao;
    @Mock private EmailService emailService;
    @Mock private BCryptPasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;

    @InjectMocks
    private TokenManagementServiceImpl tokenManagementService;

    @BeforeEach
    void setUp() {
        // Hydrate external configurations matching your class properties
        ReflectionTestUtils.setField(tokenManagementService, "refreshExpiryDays", 24);
        ReflectionTestUtils.setField(tokenManagementService, "activationExpiryHours", 24);
        ReflectionTestUtils.setField(tokenManagementService, "resetExpiryHours", 24);
    }

    // =========================================================================
    // REFRESH TOKEN TESTS
    // =========================================================================

    @Test
    void generateRefreshToken_Success() {
        // Arrange
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken("old-valid-token");

        // Uses your domain constructor to set expiresAt cleanly into the future
        RefreshTokenDocument document = new RefreshTokenDocument("old-valid-token", "test@loan.com", 24);

        when(refreshTokenDao.findByToken("old-valid-token")).thenReturn(Optional.of(document));
        when(jwtService.createRefreshToken()).thenReturn("new-secure-token");

        // Act
        String result = tokenManagementService.generateRefreshToken(request);

        // Assert
        assertEquals("new-secure-token", result);
        verify(refreshTokenDao, times(1)).deleteByUserEmail("test@loan.com");
        verify(refreshTokenDao, times(1)).save(any(RefreshTokenDocument.class));
    }

    @Test
    void generateRefreshToken_ThrowsTokenNotProvidedException_WhenTokenNotFound() {
        // Arrange
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken("missing-token");
        when(refreshTokenDao.findByToken("missing-token")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(TokenNotProvidedException.class, () ->
                tokenManagementService.generateRefreshToken(request));
    }

    @Test
    void generateRefreshToken_ThrowsUnauthorizedAccessException_WhenExpired() {
        // Arrange
        RefreshTokenRequestDto request = new RefreshTokenRequestDto();
        request.setRefreshToken("expired-token");

        // Force expiration by initializing with a negative time offset manually via Builder
        RefreshTokenDocument expiredDocument = RefreshTokenDocument.builder()
                .token("expired-token")
                .userEmail("test@loan.com")
                .expiresAt(Instant.now().minus(1, ChronoUnit.DAYS))
                .build();

        when(refreshTokenDao.findByToken("expired-token")).thenReturn(Optional.of(expiredDocument));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () ->
                tokenManagementService.generateRefreshToken(request));
        verify(refreshTokenDao, times(1)).delete(expiredDocument);
        verify(refreshTokenDao, never()).deleteByUserEmail(anyString());
    }

    // =========================================================================
    // ACTIVATION TOKEN TESTS
    // =========================================================================

    @Test
    void generateActivationToken_Success() {
        // Arrange
        String email = "test@loan.com";
        when(userDao.findByEmail(email)).thenReturn(Optional.of(new UserDocument()));

        // Act
        String token = tokenManagementService.generateActivationToken(email);

        // Assert
        assertNotNull(token);
        verify(activationTokenDao, times(1)).save(any(ActivationTokenDocument.class));
    }

    @Test
    void generateActivationToken_ThrowsAccountNotFoundException() {
        // Arrange
        String email = "ghost@loan.com";
        when(userDao.findByEmail(email)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                tokenManagementService.generateActivationToken(email));
    }

    @Test
    void verifyActivationToken_Success() {
        // Arrange
        String tokenValue = "valid-act-token";
        ActivationTokenDocument document = new ActivationTokenDocument(tokenValue, "test@loan.com", 24);

        UserDocument user = new UserDocument();
        user.setEmail("test@loan.com");
        user.setStatus(UserStatus.PENDING_VERIFICATION);

        when(activationTokenDao.findByToken(tokenValue)).thenReturn(Optional.of(document));
        when(userDao.findByEmail("test@loan.com")).thenReturn(Optional.of(user));

        // Act
        tokenManagementService.verifyActivationToken(tokenValue);

        // Assert
        assertEquals(UserStatus.ACTIVE, user.getStatus());
        verify(userDao, times(1)).save(user);
        verify(activationTokenDao, times(1)).delete(document);
    }

    @Test
    void verifyActivationToken_ThrowsUnauthorizedException_WhenExpired() {
        // Arrange
        String tokenValue = "expired-act-token";
        ActivationTokenDocument expiredDocument = ActivationTokenDocument.builder()
                .token(tokenValue)
                .userEmail("test@loan.com")
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(activationTokenDao.findByToken(tokenValue)).thenReturn(Optional.of(expiredDocument));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () ->
                tokenManagementService.verifyActivationToken(tokenValue));
        verify(activationTokenDao, times(1)).delete(expiredDocument);
        verify(userDao, never()).save(any(UserDocument.class));
    }

    // =========================================================================
    // PASSWORD RESET TESTS
    // =========================================================================

    @Test
    void generatePasswordResetToken_Success() {
        // Arrange
        String email = "test@loan.com";
        UserDocument user = new UserDocument();
        user.setUsername("aman_raj");
        when(userDao.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        String token = tokenManagementService.generatePasswordResetToken(email);

        // Assert
        assertNotNull(token);
        verify(passwordResetTokenDao, times(1)).save(any(PasswordResetTokenDocument.class));
        verify(emailService, times(1)).sendPasswordResetEmail(email, "aman_raj", token);
    }

    @Test
    void verifyPasswordResetToken_Success() {
        // Arrange
        String tokenValue = "valid-reset-token";
        PasswordResetTokenDocument document = new PasswordResetTokenDocument(tokenValue, "test@loan.com", 24);

        UserDocument user = new UserDocument();
        SecurityBlock security = new SecurityBlock();
        user.setSecurity(security);

        when(passwordResetTokenDao.findByToken(tokenValue)).thenReturn(Optional.of(document));
        when(userDao.findByEmail("test@loan.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("securePassword123")).thenReturn("encrypted-string-hash");

        // Act
        tokenManagementService.verifyPasswordResetToken(tokenValue, "securePassword123");

        // Assert
        assertEquals("encrypted-string-hash", user.getSecurity().getPasswordHash());
        verify(userDao, times(1)).save(user);
        verify(passwordResetTokenDao, times(1)).delete(document);
    }

    @Test
    void verifyPasswordResetToken_ThrowsUnauthorizedException_WhenExpired() {
        // Arrange
        String tokenValue = "expired-reset-token";
        PasswordResetTokenDocument expiredDocument = PasswordResetTokenDocument.builder()
                .token(tokenValue)
                .userEmail("test@loan.com")
                .expiresAt(Instant.now().minus(1, ChronoUnit.HOURS))
                .build();

        when(passwordResetTokenDao.findByToken(tokenValue)).thenReturn(Optional.of(expiredDocument));

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () ->
                tokenManagementService.verifyPasswordResetToken(tokenValue, "newPassword"));
        verify(passwordResetTokenDao, times(1)).delete(expiredDocument);
        verify(userDao, never()).save(any(UserDocument.class));
    }

    // =========================================================================
    // ORELSETHROW() COVERAGE TESTS
    // =========================================================================

    @Test
    void verifyActivationToken_ThrowsUnauthorizedAccessException_WhenTokenNotFound() {
        // Arrange
        String missingToken = "ghost-activation-token";
        // Simulate database lookup returning empty Optional
        when(activationTokenDao.findByToken(missingToken)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () ->
                tokenManagementService.verifyActivationToken(missingToken));

        // Verify no further database mutations occurred
        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).save(any(UserDocument.class));
    }

    @Test
    void verifyActivationToken_ThrowsAccountNotFoundException_WhenUserMissingForToken() {
        // Arrange
        String tokenValue = "valid-token-but-no-user";
        ActivationTokenDocument document = new ActivationTokenDocument(tokenValue, "corrupt-email@loan.com", 24);

        when(activationTokenDao.findByToken(tokenValue)).thenReturn(Optional.of(document));
        // Token exists, but the user account associated with it has been deleted or is missing
        when(userDao.findByEmail("corrupt-email@loan.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                tokenManagementService.verifyActivationToken(tokenValue));

        verify(userDao, never()).save(any(UserDocument.class));
        verify(activationTokenDao, never()).delete(any(ActivationTokenDocument.class));
    }

    @Test
    void verifyPasswordResetToken_ThrowsUnauthorizedAccessException_WhenTokenNotFound() {
        // Arrange
        String missingToken = "ghost-reset-token";
        when(passwordResetTokenDao.findByToken(missingToken)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UnauthorizedAccessException.class, () ->
                tokenManagementService.verifyPasswordResetToken(missingToken, "newPassword123"));

        verify(userDao, never()).findByEmail(anyString());
    }

    @Test
    void verifyPasswordResetToken_ThrowsAccountNotFoundException_WhenUserMissingForToken() {
        // Arrange
        String tokenValue = "valid-reset-but-no-user";
        PasswordResetTokenDocument document = new PasswordResetTokenDocument(tokenValue, "ghost-user@loan.com", 24);

        when(passwordResetTokenDao.findByToken(tokenValue)).thenReturn(Optional.of(document));
        // Token is found, but user validation fails because the email mapping is missing in the database
        when(userDao.findByEmail("ghost-user@loan.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                tokenManagementService.verifyPasswordResetToken(tokenValue, "newPassword123"));

        verify(passwordEncoder, never()).encode(anyString());
        verify(userDao, never()).save(any(UserDocument.class));
    }

    @Test
    void generatePasswordResetToken_ThrowsAccountNotFoundException_WhenUserNotFound() {
        // Arrange
        String missingEmail = "notfound@loan.com";
        // Simulate the database returning an empty Optional for the user search
        when(userDao.findByEmail(missingEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () ->
                tokenManagementService.generatePasswordResetToken(missingEmail));

        // Verify that no token was generated, saved, or emailed
        verify(passwordResetTokenDao, never()).save(any(PasswordResetTokenDocument.class));
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString(), anyString());
    }
}