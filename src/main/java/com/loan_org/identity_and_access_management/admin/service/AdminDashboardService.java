package com.loan_org.identity_and_access_management.admin.service;

import com.loan_org.identity_and_access_management.admin.model.dashboard.DashboardUsersSummary;

public interface AdminDashboardService {
    DashboardUsersSummary fetchUsersSummary();
}
