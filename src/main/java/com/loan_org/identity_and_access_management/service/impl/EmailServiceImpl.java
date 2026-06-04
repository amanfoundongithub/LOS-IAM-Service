package com.loan_org.identity_and_access_management.service.impl;

import com.loan_org.identity_and_access_management.config.RabbitMQConfig;
import com.loan_org.identity_and_access_management.dto.NotificationEventDto;
import com.loan_org.identity_and_access_management.service.EmailService;
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
                "borrowerName", username, // Unified with notification schema
                "activationUrl", activationUrl
        );

        NotificationEventDto event = NotificationEventDto.builder()
                .userId(username) // Or pass actual user UUID string if available
                .transactionId("iam-act-" + UUID.randomUUID().toString().substring(0, 8))
                .recipient(email)
                .channel("EMAIL")
                .templateCode("USER_ACTIVATION")
                .priority("HIGH") // Triggers the high-priority AMQP pipeline queue
                .title("Activate Your Apex Lending Account")
                .templateVariables(templateVariables)
                .build();

        dispatchToBroker(RabbitMQConfig.ROUTING_KEY_HIGH, event);
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

        dispatchToBroker(RabbitMQConfig.ROUTING_KEY_HIGH, event);
    }

    /**
     * Helper to gracefully push the structured event down the RabbitMQ wire.
     */
    private void dispatchToBroker(String routingKey, NotificationEventDto event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EXCHANGE_NAME,
                    routingKey,
                    event
            );
            log.debug("Notification event successfully placed on exchange with routing key: {}", routingKey);
        } catch (Exception e) {
            log.error("Critical failure publishing notification event to AMQP infrastructure for transaction: {}",
                    event.getTransactionId(), e);
            // Throwing a runtime exception ensures transaction boundaries fail if broker is completely reachable
            throw new RuntimeException("Messaging broker communication failure", e);
        }
    }
}