package com.loan_org.identity_and_access_management.auth.controller;

import com.loan_org.identity_and_access_management.auth.dto.PasswordChangeRequestDto;
import com.loan_org.identity_and_access_management.auth.dto.RefreshTokenRevokeDto;
import com.loan_org.identity_and_access_management.auth.service.UserManagementService;
import com.loan_org.identity_and_access_management.token.service.TokenManagementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.auth.base_url}")
public class AuthController {

    private final TokenManagementService tokenManagementService;
    private final UserManagementService  userManagementService;

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPasswordRequest(
            @RequestParam("email") String email
    ) {
        tokenManagementService.generatePasswordResetToken(email);
        return new ResponseEntity<>("Sent to:" + email, HttpStatus.ACCEPTED);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            @Valid @RequestBody RefreshTokenRevokeDto revocationRequest
    ) {
        tokenManagementService.revokeRefreshToken(revocationRequest);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordChangeRequestDto changeRequest,
            @AuthenticationPrincipal String email
    ) {
        userManagementService.updatePassword(email, changeRequest);
        return ResponseEntity.ok().build();
    }

}
