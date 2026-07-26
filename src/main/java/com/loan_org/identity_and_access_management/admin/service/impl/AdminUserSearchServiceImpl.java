package com.loan_org.identity_and_access_management.admin.service.impl;

import java.util.List;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;


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

        log.info("Received ADMIN request to search for users based on criteria: {}",
                searchAttributes.toString());

        // Sort criteria
        String sortBy = searchAttributes.sortBy();
        String sortDir = searchAttributes.sortDir();
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.DESC.name())
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        // Pageable result
        Pageable pageable = PageRequest.of(searchAttributes.page(), searchAttributes.size(), sort);

        // Sample reference
        UserDocument sampleDocument = new UserDocument();
        if (searchAttributes.role() != null) {
            sampleDocument.getAttributes().put("role", searchAttributes.role());
        }
        if (searchAttributes.status() != null){
            sampleDocument.setStatus(UserStatus.valueOf(searchAttributes.status()));
        }

        // Matcher
        ExampleMatcher matcher = ExampleMatcher.matchingAll().withIgnoreNullValues();
        Example<UserDocument> example = Example.of(sampleDocument, matcher);

        // Find all documents in the page
        Page<UserDocument> userPage = userRepository.findAll(example, pageable);
        log.info("Found {} users based on criteria: {}",
                userPage.getTotalElements(),
                userPage.toString());

        List<UserResponseDto> dtoList = userPage.getContent().stream()
                .map(this::mapToResponseDto)
                .toList();
        log.info("Filtered & found {} users based on the example: {}",
                dtoList.size(),
                example.toString());

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
                .build();
    }

}
