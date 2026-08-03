package com.loan_org.identity_and_access_management.auth.refreshToken;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan_org.identity_and_access_management.token.refresh.RefreshTokenService;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.refresh_token.url}")
public class RefreshTokenController {
    
    private final RefreshTokenService refreshTokenService;

    @PostMapping
    public ResponseEntity<String> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        return ResponseEntity.status(HttpStatus.OK)
            .body(
                refreshTokenService.generateRefreshToken(request)
            );
    }

    @PostMapping("/login")
    public ResponseEntity<String> postMethodName(
        @CookieValue(value = "refreshToken", required = false) String refreshToken
    ) {
        RefreshTokenRequest request = new RefreshTokenRequest(refreshToken);
        return ResponseEntity.status(HttpStatus.OK)
            .body(
                refreshTokenService.loginUsingRefreshToken(request)
            );
    }
    
}
