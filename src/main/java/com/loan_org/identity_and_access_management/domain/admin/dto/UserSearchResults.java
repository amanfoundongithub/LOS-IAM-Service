package com.loan_org.identity_and_access_management.domain.admin.dto;

import com.loan_org.identity_and_access_management.domain.user.dto.UserResponseDto;
import lombok.Builder;
import lombok.Data;

import java.util.List;

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
