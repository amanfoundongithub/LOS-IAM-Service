package com.loan_org.identity_and_access_management.messaging.service.impl;

import com.loan_org.identity_and_access_management.messaging.RabbitMQConfig;
import com.loan_org.identity_and_access_management.messaging.dto.NotificationEventDto;
import com.loan_org.identity_and_access_management.messaging.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * High-throughput implementation of {@link EmailService} that offloads
 * notification processing by publishing event payloads to an external RabbitMQ broker.
 *
 * @author Aman Raj
 * @since 1.0.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final RabbitTemplate rabbitTemplate;

    @Value("${app.base-url}")
    private String baseUrl;

    @Override
    public void sendActivationEmail(String email, String username, String token) {
        log.info("Publishing USER_ACTIVATION event to broker for username: {}", username);

        String activationUrl = baseUrl + "/api/v1/auth/verify?token=" + token;

        // Match the layout variables expected by your notification template engine
        Map<String, Object> templateVariables = Map.of(
                "borrowerName", username,
                "activationUrl", activationUrl
        );

        NotificationEventDto event = NotificationEventDto.builder()
                .userId(username)
                .transactionId("iam-act-" + UUID.randomUUID().toString().substring(0, 8))
                .recipient(email)
                .channel("EMAIL")
                .templateCode("USER_ACTIVATION")
                .priority("HIGH")
                .title("Activate Your Apex Lending Account")
                .templateVariables(templateVariables)
                .build();

        dispatchToBroker(event);
    }

    @Override
    public void sendPasswordResetEmail(String email, String username, String token) {
        log.info("Publishing PASSWORD_RESET event to broker for username: {}", username);

        String resetUrl = baseUrl + "/reset/password?token=" + token;

        Map<String, Object> templateVariables = Map.of(
                "borrowerName", username,
                "resetUrl", resetUrl
        );

        NotificationEventDto event = NotificationEventDto.builder()
                .userId(username)
                .transactionId("iam-pwd-" + UUID.randomUUID().toString().substring(0, 8))
                .recipient(email)
                .channel("EMAIL")
                .templateCode("PASSWORD_RESET")
                .priority("HIGH")
                .title("Reset Your Password - Apex Lending")
                .templateVariables(templateVariables)
                .build();

        dispatchToBroker(event);
    }

    /**
     * Helper to gracefully push the structured event down the RabbitMQ wire.
     */
    private void dispatchToBroker(NotificationEventDto event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    RabbitMQConfig.ROUTING_KEY_HIGH,
                    event
            );
            log.debug("Notification event successfully placed on exchange with routing key: {}", RabbitMQConfig.ROUTING_KEY_HIGH);
        } catch (Exception e) {
            log.error("Critical failure publishing notification event to AMQP infrastructure for transaction: {}",
                    event.getTransactionId(), e);
            throw new RuntimeException("Messaging broker communication failure", e);
        }
    }
}