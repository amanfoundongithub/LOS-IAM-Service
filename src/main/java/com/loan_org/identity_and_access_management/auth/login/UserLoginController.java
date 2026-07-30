package com.loan_org.identity_and_access_management.auth.login;

import com.loan_org.identity_and_access_management.auth.login.service.UserLoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.endpoint.login.url}")
@Tag(
    name = "Authentication",
    description = "Authentication endpoints for user login and token issuance."
)
public class UserLoginController {

    private final UserLoginService userLoginService;

    @Operation(
        summary = "Authenticate a user",
        description = """
                Authenticates a user using either their email address or username,
                together with their password.

                Upon successful authentication, an access token, refresh token,
                and the authenticated user's profile are returned.
                """
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "User authenticated successfully",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UserLoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Invalid username/email or password"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Access denied"
            ),
            @ApiResponse(
                    responseCode = "423",
                    description = "Account is locked"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Too many login attempts"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Internal server error"
            )
    })
    @PostMapping
    public ResponseEntity<UserLoginResponse> login(
            @Valid @RequestBody UserLoginRequest request,
                                HttpServletResponse httpResponse) {
        
        UserLoginResponse response = userLoginService.login(request);
        ResponseCookie cookie = ResponseCookie.from("refreshToken", response.refreshToken())
            .httpOnly(true)
            .secure(false)        
            .sameSite("Lax")
            .maxAge(Duration.ofDays(7))
            .build();
        httpResponse.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
        return ResponseEntity.ok(response);
    }
}