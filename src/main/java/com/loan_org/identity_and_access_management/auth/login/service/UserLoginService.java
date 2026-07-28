package com.loan_org.identity_and_access_management.auth.login.service;

import com.loan_org.identity_and_access_management.auth.login.UserLoginRequest;
import com.loan_org.identity_and_access_management.auth.login.UserLoginResponse;

public interface UserLoginService {
    UserLoginResponse login(UserLoginRequest loginRequest);
}
