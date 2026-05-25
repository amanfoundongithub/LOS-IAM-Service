package com.loan_org.identity_and_access_management.service;

import com.loan_org.identity_and_access_management.service.impl.EmailServiceImpl;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        // Inject values typically set by @Value annotations
        ReflectionTestUtils.setField(emailService, "fromEmail", "no-reply@loan.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Apex Lending");
        ReflectionTestUtils.setField(emailService, "baseUrl", "http://localhost:8080");
    }

    // =========================================================================
    // HAPPY PATHS
    // =========================================================================

    @Test
    void sendActivationEmail_Success() throws Exception {
        // Arrange
        String email = "user@example.com";
        String username = "aman_raj";
        String token = "act-123";
        String expectedHtml = "<html>Activation Email</html>";

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("activation-email"), any(Context.class))).thenReturn(expectedHtml);

        // Act
        emailService.sendActivationEmail(email, username, token);

        // Assert
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_Success() throws Exception {
        // Arrange
        String email = "user@example.com";
        String username = "aman_raj";
        String token = "reset-123";
        String expectedHtml = "<html>Reset Email</html>";

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("password-reset-email"), any(Context.class))).thenReturn(expectedHtml);

        // Act
        emailService.sendPasswordResetEmail(email, username, token);

        // Assert
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    // =========================================================================
    // EXCEPTION / RETRY TRIGGER PATHS
    // =========================================================================

    @Test
    void sendActivationEmail_ThrowsRuntimeException_OnMailException() throws Exception {
        // Arrange
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("html");

        // Simulating Spring Mail Core transport layer blowing up
        doThrow(new MailSendException("SMTP Server Unavailable")).when(javaMailSender).send(any(MimeMessage.class));

        // Act & Assert (Verifies it wraps into RuntimeException to trigger the proxy's @Retryable)
        assertThrows(RuntimeException.class, () ->
                emailService.sendActivationEmail("test@loan.com", "user", "token"));
    }

    @Test
    void sendPasswordResetEmail_ThrowsRuntimeException_OnMessagingException() throws Exception {
        // Arrange
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // Simulating Thymeleaf template compilation engine or layout parsing crashing out
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException(new MessagingException("Invalid Mime Structure Header")));

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                emailService.sendPasswordResetEmail("test@loan.com", "user", "token"));
    }

    @Test
    void sendActivationEmail_ThrowsRuntimeException_OnMessagingException() throws Exception {
        // Arrange
        String email = "user@example.com";
        String username = "aman_raj";
        String token = "act-123";

        // 1. Make this specific stubbing lenient so Mockito allows the test to finish
        lenient().when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // 2. We simulate the exception being thrown when MimeMessageHelper initializes or when template engine fails
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenAnswer(invocation -> {
                    throw new MessagingException("Invalid multi-part frame layout configuration");
                });

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                emailService.sendActivationEmail(email, username, token));
    }

    @Test
    void sendPasswordResetEmail_ThrowsRuntimeException_OnUnsupportedEncodingException() throws Exception {
        // Arrange
        String email = "user@example.com";
        String username = "aman_raj";
        String token = "reset-123";

        // 1. Make this specific stubbing lenient as well
        lenient().when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        // 2. Simulate the UnsupportedEncodingException being thrown from the underlying email helper initialization
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenAnswer(invocation -> {
                    throw new java.io.UnsupportedEncodingException("UTF-8 character configuration template mapping failed");
                });

        // Act & Assert
        assertThrows(RuntimeException.class, () ->
                emailService.sendPasswordResetEmail(email, username, token));
    }
}