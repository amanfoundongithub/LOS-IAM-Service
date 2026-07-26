package com.loan_org.identity_and_access_management.auth.register;

import com.loan_org.identity_and_access_management.auth.register.service.UserRegistrationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/register")
@Tag(
        name = "Authentication",
        description = "Authentication endpoints for user registration and account onboarding."
)
public class UserRegistrationController {

    private final UserRegistrationService userRegistrationService;

    @Operation(
            summary = "Register a new user account",
            description = """
                    Creates a new user account and initiates the registration workflow.
                    
                    Upon successful registration, an activation email is sent to the
                    registered email address. The account remains inactive until
                    email verification is completed.
                    """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "User registered successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserRegistrationResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email or username already exists"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many registration requests"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping
    public ResponseEntity<UserRegistrationResponse> register(
            @Valid @org.springframework.web.bind.annotation.RequestBody
            UserRegistrationRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(userRegistrationService.register(request));
    }
}