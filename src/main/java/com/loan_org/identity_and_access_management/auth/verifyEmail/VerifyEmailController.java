package com.loan_org.identity_and_access_management.auth.verifyEmail;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.loan_org.identity_and_access_management.token.emailActivation.EmailActivationTokenService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.verify_email.url}")
public class VerifyEmailController {

    private final EmailActivationTokenService emailActivationTokenService;
    
    @GetMapping
    public ResponseEntity<Map<String, String>> verifyAccount(
            @RequestParam("token") String token
    ) {
        emailActivationTokenService.verifyActivationToken(token);
        return ResponseEntity.status(HttpStatus.OK)
        .body(
            Map.of(
                "status", "Email has been verified successfully!"
            )
        );
    }
}
