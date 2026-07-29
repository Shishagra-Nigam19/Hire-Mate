package com.hiremate.mapper;

import com.hiremate.domain.entity.Role;
import com.hiremate.domain.entity.User;
import com.hiremate.dto.auth.UserSummaryResponse;
import com.hiremate.dto.user.UserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToStringSet")
    UserResponse toUserResponse(User user);

    @Mapping(target = "roles", source = "roles", qualifiedByName = "mapRolesToStringSet")
    UserSummaryResponse toUserSummaryResponse(User user);

    @Named("mapRolesToStringSet")
    default Set<String> mapRolesToStringSet(Set<Role> roles) {
        if (roles == null) {
            return Set.of();
        }
        return roles.stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());
    }
}
