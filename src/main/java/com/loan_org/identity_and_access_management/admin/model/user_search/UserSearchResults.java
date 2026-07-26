package com.loan_org.identity_and_access_management.admin.model.user_search;

import lombok.Builder;
import lombok.Data;

import java.util.List;

import com.loan_org.identity_and_access_management.userEntity.dto.UserResponseDto;

@Data
@Builder
public class UserSearchResults {
    private List<UserResponseDto> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean isLast;
}
