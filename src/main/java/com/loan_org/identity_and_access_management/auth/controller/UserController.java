package com.loan_org.identity_and_access_management.auth.controller;

import com.loan_org.identity_and_access_management.auth.service.UserManagementService;
import com.loan_org.identity_and_access_management.user.dto.UserResponseDto;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.user.base_url}")
public class UserController {

    private final UserManagementService userManagementService;

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> me(
            @AuthenticationPrincipal String email
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(userManagementService.fetchUserByEmail(email));
    }

}
