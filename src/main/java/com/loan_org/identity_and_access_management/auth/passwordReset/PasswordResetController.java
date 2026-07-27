package com.loan_org.identity_and_access_management.auth.passwordReset;

import com.loan_org.identity_and_access_management.token.service.TokenManagementService;
import com.loan_org.identity_and_access_management.userManagement.service.UserManagementService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.password.url}")
public class PasswordResetController {

    private final TokenManagementService tokenManagementService;
    private final UserManagementService  userManagementService;

    @PostMapping("/reset")
    public ResponseEntity<String> resetPasswordRequest(
            @RequestParam("email") String email
    ) {
        tokenManagementService.generatePasswordResetToken(email);
        return new ResponseEntity<>("Sent to:" + email, HttpStatus.ACCEPTED);
    }

    @PostMapping("/change")
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody PasswordResetRequest changeRequest,
            @AuthenticationPrincipal String email
    ) {
        userManagementService.updatePassword(email, changeRequest);
        return ResponseEntity.ok().build();
    }

}
