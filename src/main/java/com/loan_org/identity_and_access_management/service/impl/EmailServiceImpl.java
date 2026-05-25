package com.loan_org.identity_and_access_management.service.impl;

import com.loan_org.identity_and_access_management.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.retry.annotation.Backoff;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.springframework.retry.annotation.Retryable;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Robust, asynchronous implementation of the {@link EmailService} built for high-throughput
 * and resilient messaging delivery across enterprise networks.
 * <p>
 * This service leverages Spring Retry frameworks to automatically absorb transient connection drops
 * and encapsulates execution pathways inside managed background task executors.
 * </p>
 *
 * @author Aman Raj
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.mail.from-name}")
    private String fromName;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    @Async("emailTaskExecutor")
    @Retryable(
            retryFor = {RuntimeException.class},
            maxAttemptsExpression = "${app.mail.retry.max_attempts}",
            backoff = @Backoff(
                    delayExpression = "${app.mail.retry.delay_in_seconds}",
                    multiplierExpression = "#{T(java.lang.Double).parseDouble('${app.mail.retry.delay_multiplier}')}"
            )
    )
    public void sendActivationEmail(String email, String username, String token) {
        try {
            String activationUrl = baseUrl + "/api/v1/auth/verify?token=" + token;
            Map<String, Object> variables = Map.of(
                    "name", username,
                    "activationUrl", activationUrl
            );
            sendHtmlEmail(email, "Activate Your Apex Lending Account", "activation-email", variables);
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.warn("Transient delivery failure targeting activation destination [{}]. Queueing retry attempt...", email);
            throw new RuntimeException(e);
        }
    }

    @Override
    @Async("emailTaskExecutor")
    @Retryable(
            retryFor = {RuntimeException.class},
            maxAttemptsExpression = "${app.mail.retry.max_attempts}",
            backoff = @Backoff(
                    delayExpression = "${app.mail.retry.delay_in_seconds}",
                    multiplierExpression = "#{T(java.lang.Double).parseDouble('${app.mail.retry.delay_multiplier}')}"
            )
    )
    public void sendPasswordResetEmail(String email, String username, String token) {
        try {
            String resetUrl = baseUrl + "/reset/password?token=" + token;
            Map<String, Object> variables = Map.of(
                    "name", username,
                    "resetUrl", resetUrl
            );
            sendHtmlEmail(email, "Reset Your Password - Apex Lending", "password-reset-email", variables);
        } catch (MessagingException | UnsupportedEncodingException | MailException e) {
            log.warn("Transient delivery failure targeting reset destination [{}]. Queueing retry attempt...", email);
            throw new RuntimeException(e);
        }
    }

    private void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateVariables)
            throws MessagingException, UnsupportedEncodingException, MailException {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

        Context context = new Context();
        context.setVariables(templateVariables);
        String htmlContent = templateEngine.process(templateName, context);

        helper.setFrom(fromEmail, fromName);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        javaMailSender.send(mimeMessage);
        log.info("Successfully dispatched secure email [{}] to targeted destination address.", templateName);
    }
}
