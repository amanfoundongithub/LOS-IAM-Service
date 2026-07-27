package com.loan_org.identity_and_access_management.auth.refreshToken;

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
@RequestMapping("${api.endpoint.refresh_token.url}")
public class RefreshTokenController {
    
    private final TokenManagementService tokenManagementService;

    @PostMapping
    public ResponseEntity<String> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
            .body(
                tokenManagementService.generateRefreshToken(request)
            );
    }
}
