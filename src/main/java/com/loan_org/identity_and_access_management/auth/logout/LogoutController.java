package com.loan_org.identity_and_access_management.auth.logout;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan_org.identity_and_access_management.token.service.TokenManagementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/logout")
public class LogoutController {

    private final TokenManagementService tokenManagementService;
    
    @PostMapping
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest revocationRequest
    ) {
        tokenManagementService.revokeRefreshToken(revocationRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .build();
    }
}
