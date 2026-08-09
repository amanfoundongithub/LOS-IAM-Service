package com.loan_org.identity_and_access_management.admin.model.dashboard;

public record DashboardUsersSummary(
    long totalUsers,
    long activeUsers,
    long lockedUsers,
    long adminUsers
) {}