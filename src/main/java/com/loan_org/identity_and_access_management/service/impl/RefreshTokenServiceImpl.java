package com.loan_org.identity_and_access_management.service.impl;

import com.loan_org.identity_and_access_management.dao.RefreshTokenDao;
import com.loan_org.identity_and_access_management.dao.UserDao;
import com.loan_org.identity_and_access_management.dto.RefreshTokenRequestDto;
import com.loan_org.identity_and_access_management.entity.RefreshTokenDocument;
import com.loan_org.identity_and_access_management.security.JwtService;
import com.loan_org.identity_and_access_management.service.RefreshTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;

@Service
public class RefreshTokenServiceImpl implements RefreshTokenService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private RefreshTokenDao refreshTokenDao;

    @Autowired
    private JwtService jwtService;

    @Override
    public String refreshToken(RefreshTokenRequestDto request) {
        String originalToken = request.getRefreshToken();

        // Find in DB
        Optional<RefreshTokenDocument> refreshTokenDocument = refreshTokenDao.findByToken(originalToken);
        if(refreshTokenDocument.isEmpty()) {
            return "";
        }
        RefreshTokenDocument document = refreshTokenDocument.get();
        // Check if expired
        if(document.getExpiryDate().isBefore(Instant.now())) {
            return "";
        }
        // Assign new fresh one now
        return jwtService.createRefreshToken(document.getUserEmail());
    }
}
