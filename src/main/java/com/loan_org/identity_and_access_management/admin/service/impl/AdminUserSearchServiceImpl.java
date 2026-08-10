package com.loan_org.identity_and_access_management.admin.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Service;

import com.loan_org.identity_and_access_management.admin.model.user_search.UserSearchAttributes;
import com.loan_org.identity_and_access_management.admin.model.user_search.UserSearchResults;
import com.loan_org.identity_and_access_management.admin.service.AdminUserSearchService;
import com.loan_org.identity_and_access_management.userEntity.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.entity.UserStatus;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserSearchServiceImpl implements AdminUserSearchService {

    private final MongoTemplate mongoTemplate;

    @Override
    public UserSearchResults searchUsers(UserSearchAttributes searchAttributes) {

        log.info("Received ADMIN request to search users with criteria: {}", searchAttributes);

        // 1. Resolve Sort Field (Maps DTO fields to actual Mongo document nested fields if necessary)
        String sortProperty = resolveSortProperty(searchAttributes.sortBy());
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(searchAttributes.sortDir())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                sortProperty
        );

        Pageable pageable = PageRequest.of(
                searchAttributes.page(),
                searchAttributes.size(),
                sort
        );

        // 2. Build Criteria dynamically
        List<Criteria> criteriaList = new ArrayList<>();

        // Text Search (username OR email)
        if (searchAttributes.query() != null && !searchAttributes.query().isBlank()) {
            String sanitizedQuery = Pattern.quote(searchAttributes.query().trim());
            Criteria textCriteria = new Criteria().orOperator(
                    Criteria.where("username").regex(sanitizedQuery, "i"),
                    Criteria.where("email").regex(sanitizedQuery, "i")
            );
            criteriaList.add(textCriteria);
        }

        // Status Filter
        if (searchAttributes.status() != null && !searchAttributes.status().isBlank()) {
            try {
                UserStatus statusEnum = UserStatus.valueOf(searchAttributes.status().toUpperCase());
                criteriaList.add(Criteria.where("status").is(statusEnum));
            } catch (IllegalArgumentException e) {
                log.warn("Invalid user status filter provided: {}", searchAttributes.status());
            }
        }

        // Role Filter
        if (searchAttributes.role() != null) {
            criteriaList.add(Criteria.where("attributes.userRole").is(searchAttributes.role()));
        }

        // 3. Assemble Mongo Query
        Query query = new Query();
        if (!criteriaList.isEmpty()) {
            query.addCriteria(new Criteria().andOperator(criteriaList.toArray(new Criteria[0])));
        }

        // 4. Count total items before applying pagination limit/offset
        long totalCount = mongoTemplate.count(query, UserDocument.class);

        // 5. Apply Pagination and Sort to Query
        query.with(pageable);

        // 6. Execute Find Query
        List<UserDocument> userDocuments = mongoTemplate.find(query, UserDocument.class);

        // 7. Wrap into Page
        Page<UserDocument> userPage = PageableExecutionUtils.getPage(
                userDocuments,
                pageable,
                () -> totalCount
        );

        List<UserResponseDto> dtoList = userPage.getContent()
                .stream()
                .map(this::mapToResponseDto)
                .toList();

        log.info(
                "Returning {} users (page {} of {})",
                dtoList.size(),
                userPage.getNumber(),
                userPage.getTotalPages()
        );

        return UserSearchResults.builder()
                .content(dtoList)
                .pageNumber(userPage.getNumber())
                .pageSize(userPage.getSize())
                .totalElements(userPage.getTotalElements())
                .totalPages(userPage.getTotalPages())
                .isLast(userPage.isLast())
                .build();
    }

    private String resolveSortProperty(String sortBy) {
        if (sortBy == null || sortBy.isBlank()) {
            return "username";
        }
        return switch (sortBy) {
            case "createdDate" -> "metadata.createdAt";
            case "lastLoginDate" -> "metadata.lastLoginAt";
            case "role" -> "attributes.userRole";
            default -> sortBy;
        };
    }

    private UserResponseDto mapToResponseDto(UserDocument userDocument) {
        return UserResponseDto.builder()
                .id(userDocument.getId())
                .email(userDocument.getEmail())
                .username(userDocument.getUsername())
                .status(userDocument.getStatus())
                .attributes(userDocument.getAttributes())
                .createdDate(userDocument.getMetadata() != null ? userDocument.getMetadata().getCreatedAt() : null)
                .lastLoginDate(userDocument.getMetadata() != null ? userDocument.getMetadata().getLastLoginAt() : null)
                .build();
    }
}