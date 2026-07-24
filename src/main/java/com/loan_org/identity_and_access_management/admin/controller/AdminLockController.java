package com.loan_org.identity_and_access_management.admin.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;

import com.loan_org.identity_and_access_management.admin.model.account_lock.UserAccountLockRequest;
import com.loan_org.identity_and_access_management.admin.model.account_unlock.UserAccountUnlockRequest;
import com.loan_org.identity_and_access_management.admin.service.AdminLockService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.admin.base_url}")
public class AdminLockController {
    
    private final AdminLockService adminLockService;

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

}
