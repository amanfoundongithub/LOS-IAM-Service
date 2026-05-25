package com.loan_org.identity_and_access_management.controller;

import com.loan_org.identity_and_access_management.dto.*;
import com.loan_org.identity_and_access_management.entity.UserDocument;
import com.loan_org.identity_and_access_management.security.JwtService;
import com.loan_org.identity_and_access_management.service.AuthService;
import com.loan_org.identity_and_access_management.service.TokenManagementService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("${info.base_url}")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenManagementService tokenManagementService;

    @PostMapping("/register")
    public ResponseEntity<UserDocument> createRoute(@Valid @RequestBody UserRegistrationDto registration) {
        UserDocument document = authService.register(registration);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody UserLoginDto loginRequest) {
        UserResponseDto response = authService.login(loginRequest);
        String token = jwtService.generateToken(response);
        String newAccessToken = jwtService.createRefreshToken();
        return new ResponseEntity<>(new AuthResponseDto(token, newAccessToken, response), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        return new ResponseEntity<>(tokenManagementService.generateRefreshToken(request), HttpStatus.CREATED);
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verifyAccount(@RequestParam("token") String token) {
        tokenManagementService.verifyActivationToken(token);
        return new ResponseEntity<>("Account successfully activated! You can now log in.", HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPasswordRequest(@RequestParam("email") String email) {
        tokenManagementService.generatePasswordResetToken(email);
        return new ResponseEntity<>("Sent to:" + email, HttpStatus.CREATED);
    }

}
