package com.loan_org.identity_and_access_management.auth.logout;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan_org.identity_and_access_management.token.refresh.RefreshTokenService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.logout.url}")
public class LogoutController {

    private final RefreshTokenService refreshTokenService;
    
    @PostMapping
    public ResponseEntity<Void> logout(
            @Valid @RequestBody LogoutRequest revocationRequest
    ) {
        refreshTokenService.revokeRefreshToken(revocationRequest);
        return ResponseEntity.status(HttpStatus.NO_CONTENT)
        .build();
    }
}
