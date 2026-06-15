package com.loan_org.identity_and_access_management.domain.admin.controller;

import com.loan_org.identity_and_access_management.domain.admin.dto.UserSearchAttributes;
import com.loan_org.identity_and_access_management.domain.admin.dto.UserSearchResults;
import com.loan_org.identity_and_access_management.domain.admin.service.AdminUserService;
import com.loan_org.identity_and_access_management.domain.user.entity.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.admin.base_url}")
public class AdminController {

    private final AdminUserService adminUserService;

    @GetMapping("/users")
    public ResponseEntity<UserSearchResults> users(
            @RequestParam(value = "page", defaultValue = "0", required = false) int page,
            @RequestParam(value = "size", defaultValue = "10", required = false) int size,
            @RequestParam(value = "sortBy", defaultValue = "id", required = false) String sortBy,
            @RequestParam(value = "sortDir", defaultValue = "asc", required = false) String sortDir,
            @RequestParam(value = "role", required = false) UserRole role,
            @RequestParam(value = "status", required = false) String status
    ) {
        // Create a search attribute
        UserSearchAttributes searchAttributes = UserSearchAttributes.builder()
                .page(page)
                .size(size)
                .sortBy(sortBy)
                .sortDir(sortDir)
                .role(role)
                .status(status)
                .build();

        // Return the search results
        return ResponseEntity.ok(adminUserService.searchUsers(searchAttributes));
    }


}
