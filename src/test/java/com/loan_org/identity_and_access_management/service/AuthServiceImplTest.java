package com.loan_org.identity_and_access_management.service;

import com.loan_org.identity_and_access_management.dao.UserDao;
import com.loan_org.identity_and_access_management.dto.UserLoginDto;
import com.loan_org.identity_and_access_management.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.entity.*;
import com.loan_org.identity_and_access_management.exception.AccountAlreadyExistsException;
import com.loan_org.identity_and_access_management.exception.AccountNotFoundException;
import com.loan_org.identity_and_access_management.exception.UnauthorizedAccessException;
import com.loan_org.identity_and_access_management.service.impl.AuthServiceImpl;
import com.loan_org.identity_and_access_management.util.UserAttributeFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private TokenManagementService tokenManagementService;
    @Mock
    private EmailService emailService;
    @Mock
    private UserAttributeFactory userAttributeFactory;
    @Mock
    private UserDao userDao;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private MockedStatic<Instant> mockedInstant;
    private MockedStatic<TransactionSynchronizationManager> mockedSyncManager;
    private Instant fixedInstant;

    @BeforeEach
    void setUp() {
        // Set environment property configurations injected via @Value
        ReflectionTestUtils.setField(authService, "maxAttempts", 5);
        ReflectionTestUtils.setField(authService, "lockoutMinutes", 15);

        // Freeze time to cleanly test dynamic Duration math blocks
        fixedInstant = Instant.parse("2026-05-26T00:00:00Z");
        mockedInstant = mockStatic(Instant.class, CALLS_REAL_METHODS);
        mockedInstant.when(Instant::now).thenReturn(fixedInstant);

        // Static tracking mock for Spring Transaction intercept management
        mockedSyncManager = mockStatic(TransactionSynchronizationManager.class, CALLS_REAL_METHODS);
    }

    @AfterEach
    void tearDown() {
        mockedInstant.close();
        mockedSyncManager.close();
    }

    // =========================================================================
    // REGISTRATION TEST CASES
    // =========================================================================

    @Test
    void register_Success_WithActiveTransaction() {
        // Arrange
        UserRegistrationDto dto = createRegistrationDto();
        when(userDao.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userDao.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.getPassword())).thenReturn("hashedPassword");
        when(userAttributeFactory.buildRegistrationAttributes(dto)).thenReturn(Collections.emptyMap());
        when(tokenManagementService.generateActivationToken(dto.getEmail())).thenReturn("token123");

        // 1. Force the check to evaluate to true
        mockedSyncManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(true);

        // 2. Fix: Mock the registration method call to do absolutely nothing when called
        mockedSyncManager.when(() -> TransactionSynchronizationManager.registerSynchronization(any(TransactionSynchronization.class)))
                .thenAnswer(invocation -> null);

        UserDocument savedDoc = UserDocument.builder()
                .email(dto.getEmail())
                .username(dto.getUsername())
                .build();
        when(userDao.save(any(UserDocument.class))).thenReturn(savedDoc);

        // Act
        UserDocument result = authService.register(dto);

        // Assert
        assertNotNull(result);
        assertEquals(dto.getEmail(), result.getEmail());

        // 3. Capture the transaction hook from the static interceptor and run it manually
        ArgumentCaptor<TransactionSynchronization> syncCaptor = ArgumentCaptor.forClass(TransactionSynchronization.class);
        mockedSyncManager.verify(() -> TransactionSynchronizationManager.registerSynchronization(syncCaptor.capture()));

        // This fully executes and covers your email service dispatch code line block!
        syncCaptor.getValue().afterCommit();
        verify(emailService).sendActivationEmail(dto.getEmail(), dto.getUsername(), "token123");
    }

    @Test
    void register_Success_WithoutTransactionSynchronization() {
        // Arrange
        UserRegistrationDto dto = createRegistrationDto();
        when(userDao.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userDao.findByUsername(dto.getUsername())).thenReturn(Optional.empty());
        mockedSyncManager.when(TransactionSynchronizationManager::isSynchronizationActive).thenReturn(false);
        when(tokenManagementService.generateActivationToken(dto.getEmail())).thenReturn("token123");
        when(userDao.save(any(UserDocument.class))).thenReturn(UserDocument.builder().email(dto.getEmail()).username(dto.getUsername()).build());

        // Act
        authService.register(dto);

        // Assert
        verify(emailService).sendActivationEmail(dto.getEmail(), dto.getUsername(), "token123");
    }

    @Test
    void register_ThrowsException_WhenEmailAlreadyExists() {
        UserRegistrationDto dto = createRegistrationDto();
        when(userDao.findByEmail(dto.getEmail())).thenReturn(Optional.of(new UserDocument()));

        assertThrows(AccountAlreadyExistsException.class, () -> authService.register(dto));
        verify(userDao, never()).save(any());
    }

    @Test
    void register_ThrowsException_WhenUsernameAlreadyExists() {
        UserRegistrationDto dto = createRegistrationDto();
        when(userDao.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(userDao.findByUsername(dto.getUsername())).thenReturn(Optional.of(new UserDocument()));

        assertThrows(AccountAlreadyExistsException.class, () -> authService.register(dto));
    }

    // =========================================================================
    // LOGIN ROUTING TEST CASES
    // =========================================================================

    @Test
    void login_Success_ViaEmail() {
        UserLoginDto request = new UserLoginDto();
        request.setEmail("test@loan.com");
        request.setPassword("password");

        UserDocument mockUser = createActiveUser();
        when(userDao.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.getPassword(), mockUser.getSecurity().getPasswordHash())).thenReturn(true);

        UserResponseDto response = authService.login(request);
        assertNotNull(response);
        verify(userDao).save(mockUser);
    }

    @Test
    void login_Success_ViaUsername() {
        UserLoginDto request = new UserLoginDto();
        request.setUsername("aman_raj");
        request.setPassword("password");

        UserDocument mockUser = createActiveUser();
        when(userDao.findByUsername(request.getUsername())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.getPassword(), mockUser.getSecurity().getPasswordHash())).thenReturn(true);

        UserResponseDto response = authService.login(request);
        assertNotNull(response);
    }

    @Test
    void login_ThrowsException_WhenEmailNotFound() {
        UserLoginDto request = new UserLoginDto();
        request.setEmail("notfound@loan.com");

        when(userDao.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void login_ThrowsException_WhenUsernameNotFound() {
        UserLoginDto request = new UserLoginDto();
        request.setUsername("unknown_user");

        when(userDao.findByUsername(request.getUsername())).thenReturn(Optional.empty());
        assertThrows(AccountNotFoundException.class, () -> authService.login(request));
    }

    @Test
    void login_ThrowsException_WhenNoIdentifiersProvided() {
        UserLoginDto request = new UserLoginDto(); // Both null/blank
        assertThrows(AccountNotFoundException.class, () -> authService.login(request));
    }

    // =========================================================================
    // SECURITY AUTHENTICATION & LOCKOUT BRANCH TEST CASES
    // =========================================================================

    @Test
    void login_ThrowsException_WhenUserIsSuspended() {
        UserLoginDto request = new UserLoginDto();
        request.setEmail("test@loan.com");

        UserDocument suspendedUser = createActiveUser();
        suspendedUser.setStatus(UserStatus.SUSPENDED);

        when(userDao.findByEmail(request.getEmail())).thenReturn(Optional.of(suspendedUser));
        assertThrows(UnauthorizedAccessException.class, () -> authService.login(request));
    }

    @Test
    void login_ThrowsException_WhenAccountLocked_MinutesRemaining() {
        UserLoginDto request = new UserLoginDto();
        request.setEmail("test@loan.com");

        UserDocument lockedUser = createActiveUser();
        // Lockout expires exactly 5 minutes into the future
        lockedUser.getSecurity().setLockoutUntil(fixedInstant.plusSeconds(300));

        when(userDao.findByEmail(request.getEmail())).thenReturn(Optional.of(lockedUser));
        assertThrows(UnauthorizedAccessException.class, () -> authService.login(request));
    }

    @Test
    void login_ThrowsException_WhenAccountLocked_SecondsRemaining() {
        UserLoginDto request = new UserLoginDto();
        request.setEmail("test@loan.com");

        UserDocument lockedUser = createActiveUser();
        // Lockout expires exactly 45 seconds into the future (under 1 minute)
        lockedUser.getSecurity().setLockoutUntil(fixedInstant.plusSeconds(45));

        when(userDao.findByEmail(request.getEmail())).thenReturn(Optional.of(lockedUser));
        assertThrows(UnauthorizedAccessException.class, () -> authService.login(request));
    }

    @Test
    void login_HandlesFailedAttempt_IncrementsAttemptsCounter() {
        UserLoginDto request = new UserLoginDto();
        request.setEmail("test@loan.com");
        request.setPassword("wrong_pass");

        UserDocument mockUser = createActiveUser();
        mockUser.getSecurity().setFailedLoginAttempts(2); // Attempt will become 3

        when(userDao.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class, () -> authService.login(request));
        assertEquals(3, mockUser.getSecurity().getFailedLoginAttempts());
        verify(userDao).save(mockUser);
    }

    @Test
    void login_HandlesFailedAttempt_TriggersLockoutWhenThresholdMet() {
        UserLoginDto request = new UserLoginDto();
        request.setEmail("test@loan.com");
        request.setPassword("wrong_pass");

        UserDocument mockUser = createActiveUser();
        mockUser.getSecurity().setFailedLoginAttempts(4); // 4 + 1 = 5 (Triggers Max Attempts Limit)

        when(userDao.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(UnauthorizedAccessException.class, () -> authService.login(request));
        assertEquals(5, mockUser.getSecurity().getFailedLoginAttempts());
        assertNotNull(mockUser.getSecurity().getLockoutUntil());
        assertEquals(fixedInstant.plusSeconds(15 * 60L), mockUser.getSecurity().getLockoutUntil());
    }

    @Test
    void login_ThrowsException_WhenEmailIsNullAndUsernameIsNull() {
        // Arrange: Both identifiers are null to hit the final fallback 'else' block
        UserLoginDto request = new UserLoginDto();
        request.setEmail(null);
        request.setUsername(null);
        request.setPassword("password");

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> authService.login(request));
        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).findByUsername(anyString());
    }

    @Test
    void login_ThrowsException_WhenEmailIsNullAndUsernameIsBlank() {
        // Arrange: Email is null, but username exists as an empty white space string
        UserLoginDto request = new UserLoginDto();
        request.setEmail(null);
        request.setUsername("   ");
        request.setPassword("password");

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> authService.login(request));
        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).findByUsername(anyString());
    }

    @Test
    void login_ThrowsException_WhenEmailIsBlankAndUsernameIsNull() {
        // Arrange: Email is a blank string (fails the first if check), username is null (fails the else-if check)
        UserLoginDto request = new UserLoginDto();
        request.setEmail("");
        request.setUsername(null);
        request.setPassword("password");

        // Act & Assert
        assertThrows(AccountNotFoundException.class, () -> authService.login(request));
        verify(userDao, never()).findByEmail(anyString());
        verify(userDao, never()).findByUsername(anyString());
    }

    @Test
    void login_Success_WhenLockoutTimeHasExpired() {
        // Arrange: Lockout time is in the PAST relative to our fixed baseline time
        UserLoginDto request = new UserLoginDto();
        request.setEmail("test@loan.com");
        request.setPassword("password");

        UserDocument mockUser = createActiveUser();
        // Lockout expired 5 minutes ago
        mockUser.getSecurity().setLockoutUntil(fixedInstant.minusSeconds(300));

        when(userDao.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.getPassword(), mockUser.getSecurity().getPasswordHash())).thenReturn(true);

        // Act
        UserResponseDto response = authService.login(request);

        // Assert: It should bypass the lockout block, execute successfully, and clear the expired lock
        assertNotNull(response);
        assertNull(mockUser.getSecurity().getLockoutUntil());
        verify(userDao).save(mockUser);
    }

    // =========================================================================
    // DATA GENERATION BUILDERS
    // =========================================================================

    private UserRegistrationDto createRegistrationDto() {
        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setEmail("test@loan.com");
        dto.setUsername("aman_raj");
        dto.setPassword("rawPassword");
        return dto;
    }

    private UserDocument createActiveUser() {
        return UserDocument.builder()
                .id("MDB-101")
                .email("test@loan.com")
                .username("aman_raj")
                .status(UserStatus.PENDING_VERIFICATION)
                .security(SecurityBlock.builder()
                        .passwordHash("hashedPassword")
                        .failedLoginAttempts(0)
                        .lockoutUntil(null)
                        .build())
                .metadata(MetadataBlock.builder().build())
                .build();
    }
}