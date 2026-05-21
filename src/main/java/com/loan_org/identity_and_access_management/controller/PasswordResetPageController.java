package com.loan_org.identity_and_access_management.controller;

import com.loan_org.identity_and_access_management.service.TokenManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("${info.base_url}")
public class PasswordResetPageController {

    @Autowired
    private TokenManagementService tokenManagementService;

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
