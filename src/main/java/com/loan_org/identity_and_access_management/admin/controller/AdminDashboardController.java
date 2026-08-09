package com.loan_org.identity_and_access_management.admin.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan_org.identity_and_access_management.admin.model.dashboard.DashboardUsersSummary;
import com.loan_org.identity_and_access_management.admin.service.AdminDashboardService;
import com.loan_org.identity_and_access_management.middleware.UserPrincipal;

import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;



@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    @GetMapping("/summary")
    public ResponseEntity<DashboardUsersSummary> summary(
        @AuthenticationPrincipal UserPrincipal lockerId
    ) {
        return ResponseEntity.status(HttpStatus.OK)
        .body(
            adminDashboardService.fetchUsersSummary()
        );
    }
    
    
}
