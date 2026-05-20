package com.loan_org.identity_and_access_management.service.impl;

import com.loan_org.identity_and_access_management.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.UnsupportedEncodingException;
import java.util.Map;

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
    @Async
    public void sendActivationEmail(String email, String username, String token) {
        String activationUrl = baseUrl + "/api/v1/auth/verify-activation?token=" + token;
        Map<String, Object> variables = Map.of(
                "name", username,
                "activationUrl", activationUrl
        );
        sendHtmlEmail(email, "Activate Your Apex Lending Account", "activation-email", variables);
    }

    @Override
    public void sendPasswordResetEmail(String email, String username, String token) {
        String resetUrl = baseUrl + "/reset-password?token=" + token;
        Map<String, Object> variables = Map.of(
                "name", username,
                "resetUrl", resetUrl
        );
        sendHtmlEmail(email, "Reset Your Password - Apex Lending", "password-reset-email", variables);
    }

    private void sendHtmlEmail(String to, String subject, String templateName, Map<String, Object> templateVariables) {
        try {
            MimeMessage mimeMessage = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, "UTF-8");

            // Compile template variables
            Context context = new Context();
            context.setVariables(templateVariables);
            String htmlContent = templateEngine.process(templateName, context);

            // Configure message settings
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            javaMailSender.send(mimeMessage);
            log.info("Successfully dispatched secure email [{}] to targeted destination address.", templateName);

        } catch (MessagingException | UnsupportedEncodingException e) {
            log.error("Failed to compile or transmit systemic email layout to destination address: {}", to, e);
            // In enterprise environments, write a retry message or push to a dead-letter queue (DLQ) here
        }
    }
}
