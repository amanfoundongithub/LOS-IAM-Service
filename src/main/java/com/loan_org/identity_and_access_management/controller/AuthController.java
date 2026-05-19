package com.loan_org.identity_and_access_management.controller;

import com.loan_org.identity_and_access_management.dto.AuthResponseDto;
import com.loan_org.identity_and_access_management.dto.RefreshTokenRequestDto;
import com.loan_org.identity_and_access_management.dto.UserRegistrationDto;
import com.loan_org.identity_and_access_management.dto.UserResponseDto;
import com.loan_org.identity_and_access_management.entity.UserDocument;
import com.loan_org.identity_and_access_management.security.JwtService;
import com.loan_org.identity_and_access_management.service.AuthService;
import com.loan_org.identity_and_access_management.service.RefreshTokenService;
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
    private RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<UserDocument> createRoute(@Valid @RequestBody UserRegistrationDto registration) {
        UserDocument document = authService.register(registration);
        return new ResponseEntity<>(document, HttpStatus.CREATED);
    }

    @GetMapping("/userByEmail")
    public ResponseEntity<AuthResponseDto> loginByEmail(@RequestParam("email") String email,
                                                        @RequestParam("password") String password) {
        UserResponseDto response = authService.loginWithEmail(email, password);
        String token = jwtService.generateToken(response);
        String newAccessToken = jwtService.createRefreshToken(email);
        return new ResponseEntity<>(new AuthResponseDto(token, newAccessToken, response), HttpStatus.OK);
    }

    @GetMapping("/userByUsername")
    public ResponseEntity<AuthResponseDto> loginByUsername(@RequestParam("username") String username,
                                                           @RequestParam("password") String password) {
        UserResponseDto response = authService.loginWithUsername(username, password);
        String token = jwtService.generateToken(response);
        String newAccessToken = jwtService.createRefreshToken(username);
        return new ResponseEntity<>(new AuthResponseDto(token, newAccessToken, response), HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshToken(@Valid @RequestBody RefreshTokenRequestDto request) {
        return new ResponseEntity<>(refreshTokenService.refreshToken(request), HttpStatus.CREATED);
    }
}
