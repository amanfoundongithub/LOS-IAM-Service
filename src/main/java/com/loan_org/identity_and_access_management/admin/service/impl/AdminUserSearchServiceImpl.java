package com.loan_org.identity_and_access_management.admin.service.impl;

import java.util.HashMap;
import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.loan_org.identity_and_access_management.admin.model.user_search.UserSearchAttributes;
import com.loan_org.identity_and_access_management.admin.model.user_search.UserSearchResults;
import com.loan_org.identity_and_access_management.admin.service.AdminUserSearchService;
import com.loan_org.identity_and_access_management.userEntity.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.entity.UserStatus;
import com.loan_org.identity_and_access_management.userEntity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminUserSearchServiceImpl implements AdminUserSearchService {

    private final UserRepository userRepository;

    @Override
    public UserSearchResults searchUsers(UserSearchAttributes searchAttributes) {

        log.info("Received ADMIN request to search users with criteria: {}", searchAttributes);

        // Sort
        Sort sort = Sort.by(
                "desc".equalsIgnoreCase(searchAttributes.sortDir())
                        ? Sort.Direction.DESC
                        : Sort.Direction.ASC,
                searchAttributes.sortBy()
        );

        Pageable pageable = PageRequest.of(
                searchAttributes.page(),
                searchAttributes.size(),
                sort
        );

        Page<UserDocument> userPage;

        boolean hasRoleFilter = searchAttributes.role() != null;
        boolean hasStatusFilter =
                searchAttributes.status() != null &&
                !searchAttributes.status().isBlank();

        // No filters -> return all users
        if (!hasRoleFilter && !hasStatusFilter) {

            userPage = userRepository.findAll(pageable);

        } else {

            UserDocument probe = new UserDocument();

            if (hasStatusFilter) {
                probe.setStatus(UserStatus.valueOf(searchAttributes.status().toUpperCase()));
            }

            if (hasRoleFilter) {
                probe.setAttributes(new HashMap<>());
                probe.getAttributes().put("role", searchAttributes.role());
            }

            ExampleMatcher matcher = ExampleMatcher.matchingAll()
                    .withIgnoreNullValues();

            Example<UserDocument> example = Example.of(probe, matcher);

            userPage = userRepository.findAll(example, pageable);
        }

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

    private UserResponseDto mapToResponseDto(UserDocument userDocument) {
        return UserResponseDto.builder()
                .id(userDocument.getId())
                .email(userDocument.getEmail())
                .username(userDocument.getUsername())
                .status(userDocument.getStatus())
                .attributes(userDocument.getAttributes())
                .createdDate(userDocument.getMetadata().getCreatedAt())
                .lastLoginDate(userDocument.getMetadata().getLastLoginAt())
                .build();
    }
}