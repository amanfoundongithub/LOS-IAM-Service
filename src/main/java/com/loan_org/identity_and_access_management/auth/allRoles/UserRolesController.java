package com.loan_org.identity_and_access_management.auth.allRoles;

import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.loan_org.identity_and_access_management.userEntity.entity.UserRole;

@RestController
@RequestMapping("${api.endpoint.allRoles.url}")
public class UserRolesController {
    
    @GetMapping
    public ResponseEntity<List<UserRole>> allUserStatus() {
        return ResponseEntity.status(HttpStatus.OK)
            .body(
                Arrays.asList(UserRole.values())
            );
    }

}
