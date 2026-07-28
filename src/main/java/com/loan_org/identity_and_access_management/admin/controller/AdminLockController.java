package com.loan_org.identity_and_access_management.admin.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.loan_org.identity_and_access_management.admin.model.account_lock.UserAccountLockRequest;
import com.loan_org.identity_and_access_management.admin.model.account_unlock.UserAccountUnlockRequest;
import com.loan_org.identity_and_access_management.admin.model.user_search.UserSearchAttributes;
import com.loan_org.identity_and_access_management.admin.model.user_search.UserSearchResults;
import com.loan_org.identity_and_access_management.admin.service.AdminLockService;
import com.loan_org.identity_and_access_management.admin.service.AdminUserSearchService;
import com.loan_org.identity_and_access_management.userEntity.entity.UserRole;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.admin.url}")
public class AdminLockController {
    
    private final AdminLockService adminLockService;
    private final AdminUserSearchService adminUserSearchService;

    @PostMapping("/lock")
    public ResponseEntity<Map<String, String>> lock(
            @Valid @RequestBody UserAccountLockRequest lockRequest,
            @AuthenticationPrincipal String lockerId
    ) {
        String message = adminLockService.lockUser(lockerId, lockRequest);
        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of(
                        "message", message
                )
        );
    } 

    @PostMapping("/unlock")
    public ResponseEntity<Map<String, String>> unlock(
            @Valid @RequestBody UserAccountUnlockRequest lockRequest,
            @AuthenticationPrincipal String unlockerId
    ) {
        String message = adminLockService.unlockUser(unlockerId, lockRequest);
        return ResponseEntity.status(HttpStatus.OK).body(
                Map.of(
                        "message", message
                )
        );
    }

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
        return ResponseEntity.ok(adminUserSearchService.searchUsers(searchAttributes));
    } 

}
