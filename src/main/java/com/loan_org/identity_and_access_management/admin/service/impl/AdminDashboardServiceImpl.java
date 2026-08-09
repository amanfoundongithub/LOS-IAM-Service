package com.loan_org.identity_and_access_management.admin.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;

import com.loan_org.identity_and_access_management.admin.model.dashboard.DashboardUsersSummary;
import com.loan_org.identity_and_access_management.admin.service.AdminDashboardService;
import com.loan_org.identity_and_access_management.userEntity.entity.UserDocument;
import com.loan_org.identity_and_access_management.userEntity.entity.UserStatus;
import com.loan_org.identity_and_access_management.userEntity.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final UserRepository userRepository;

    @Override
    public DashboardUsersSummary fetchUsersSummary() {
        log.info("Fetching all the users details for the admin page...");
        List<UserDocument> listOfAllUsers = userRepository.findAll();
        log.info("Fetched all users now finding the count.");
        // All users 
        long totalUsers = listOfAllUsers.size();
        // All blocked
        long blockedUsers = listOfAllUsers.stream()
        .filter(user -> user.getStatus().equals(UserStatus.LOCKED))
        .count();
        long activeUsers = totalUsers - blockedUsers;

        // Admin users
        long adminUsers = listOfAllUsers.stream()
        .filter(user -> user.getAttributes().get("userRole").toString().contains("ADMIN"))
        .count();

        return new DashboardUsersSummary(
            totalUsers,
            blockedUsers,
            activeUsers,
            adminUsers
        );
    }
    
}
