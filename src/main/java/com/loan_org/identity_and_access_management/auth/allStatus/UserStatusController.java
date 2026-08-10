package com.loan_org.identity_and_access_management.auth.allStatus;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan_org.identity_and_access_management.userEntity.entity.UserStatus;

@RestController
@RequestMapping("${api.endpoint.allStatus.url}")
public class UserStatusController {
    
    @GetMapping
    public ResponseEntity<List<UserStatus>> allUserStatus() {
        return ResponseEntity.status(HttpStatus.OK)
            .body(
                Arrays.asList(UserStatus.values())
            );
    }
    
}
