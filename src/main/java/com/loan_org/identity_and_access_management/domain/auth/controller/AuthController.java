package com.loan_org.identity_and_access_management.domain.auth.controller;

import com.loan_org.identity_and_access_management.domain.auth.dto.AuthResponseDto;
import com.loan_org.identity_and_access_management.domain.auth.dto.UserLoginDto;
import com.loan_org.identity_and_access_management.domain.auth.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.domain.token.dto.RefreshTokenRequestDto;
import com.loan_org.identity_and_access_management.domain.token.dto.RefreshTokenRevokeDto;
import com.loan_org.identity_and_access_management.domain.user.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.domain.user.entity.UserDocument;
import com.loan_org.identity_and_access_management.domain.auth.service.JwtService;
import com.loan_org.identity_and_access_management.domain.auth.service.AuthService;
import com.loan_org.identity_and_access_management.domain.token.service.TokenManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.base_url}")
public class AuthController {

    private final AuthService            authService;
    private final JwtService             jwtService;
    private final TokenManagementService tokenManagementService;

    @PostMapping("/register")
    public ResponseEntity<UserDocument> register(
            @Valid @RequestBody UserRegistrationDto registration
    ) {
        UserDocument document = authService.register(registration);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @Valid @RequestBody UserLoginDto loginRequest
    ) {
        UserResponseDto response = authService.login(loginRequest);
        String accessToken       = jwtService.generateToken(response);
        String refreshToken      = tokenManagementService.generateRefreshToken(response.getEmail());
        return new ResponseEntity<>(new AuthResponseDto(accessToken, refreshToken, response), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(
            @Valid @RequestBody RefreshTokenRequestDto request
    ) {
        return new ResponseEntity<>(tokenManagementService.generateRefreshToken(request), HttpStatus.OK);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(
            @RequestParam("token") String token
    ) {
        tokenManagementService.verifyActivationToken(token);
        return new ResponseEntity<>("Account successfully activated! You can now log in.", HttpStatus.OK);
    }

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

}
