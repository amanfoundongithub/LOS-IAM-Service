package com.loan_org.identity_and_access_management.domain.user.factory;

import com.loan_org.identity_and_access_management.domain.user.entity.UserRole;
import com.loan_org.identity_and_access_management.domain.user.factory.roles.UserRoleAttributeAssigner;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Component
public class UserAttributeFactory {

    private final Map<UserRole, UserRoleAttributeAssigner> strategyMap;

    public UserAttributeFactory(List<UserRoleAttributeAssigner> assigners) {
        this.strategyMap = assigners.stream()
                .collect(Collectors.toMap(
                        UserRoleAttributeAssigner::getRole,
                        Function.identity()
                ));
    }

    public Map<String, Object> getAttributes(UserRole role) {
        if(strategyMap.containsKey(role)) {
            return strategyMap.get(role).assign();
        } else {
            return Map.of();
        }
    }

}
