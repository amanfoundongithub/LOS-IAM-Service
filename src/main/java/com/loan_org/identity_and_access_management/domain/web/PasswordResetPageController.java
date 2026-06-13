package com.loan_org.identity_and_access_management.domain.web;

import com.loan_org.identity_and_access_management.service.TokenManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
@RequestMapping("${info.base_url}")
public class PasswordResetPageController {

    private final TokenManagementService tokenManagementService;

    @GetMapping("/reset/password")
    public String showResetPasswordForm(@RequestParam("token") String token, Model model) {
        model.addAttribute("token", token);
        return "password-reset-form";
    }

    @GetMapping("/reset/password/execute-reset")
    public String handlePasswordForm(@RequestParam("token") String token,
                                     @RequestParam("newPassword") String newPassword,
                                     Model model) {
        try {
            tokenManagementService.verifyPasswordResetToken(token, newPassword);
            return "password-reset-success";
        } catch (Exception e) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", e.getMessage());
            return "reset-form";
        }
    }

}
