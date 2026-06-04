package com.loan_org.identity_and_access_management.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEventDto {
    private String userId;
    private String transactionId;
    private String recipient;
    private String channel;
    private String templateCode;
    private String priority;
    private String title;
    private Map<String, Object> templateVariables;
}