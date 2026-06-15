package com.loan_org.identity_and_access_management.domain.admin.service.impl;

import com.loan_org.identity_and_access_management.domain.admin.dto.UserSearchAttributes;
import com.loan_org.identity_and_access_management.domain.admin.dto.UserSearchResults;
import com.loan_org.identity_and_access_management.domain.admin.service.AdminUserService;
import com.loan_org.identity_and_access_management.domain.user.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.domain.user.entity.UserStatus;
import com.loan_org.identity_and_access_management.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserServiceImpl implements AdminUserService {

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
