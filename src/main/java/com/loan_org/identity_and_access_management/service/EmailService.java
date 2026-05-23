package com.loan_org.identity_and_access_management.service;

public interface EmailService {
    public void sendActivationEmail(String email, String username, String token);
    public void sendPasswordResetEmail(String toEmail, String name, String token);
}
