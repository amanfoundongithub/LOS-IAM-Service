package com.loan_org.identity_and_access_management.domain.audit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(collection = "user_account_modification_audit")
public class UserAccountModificationAuditDocument {

    @Id
    private String id;

    /**
     * Account affected.
     * Note: ONLY EMAIL IS CONSIDERED, SINCE IT IS CONSIDERED UNIQUE
     */
    private String affectedUser;

    /**
     * Modified variables list tracking chronological alteration
     */
    private List<ModificationListEntity> modificationList;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ModificationListEntity {

        private Instant modificationDate;          // Modification date
        private String  modificationDoneBy;        // Modification done by
        private String  modifiedAttribute;         // Modified attribute
        private Object  originalValue;             // Original values
        private Object  newValue;                  // New values
        private String  modificationReason;        // Reason for modification

    }

}
