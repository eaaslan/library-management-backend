package tr.com.eaaslan.library.model.mapper;

import org.mapstruct.*;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserRole;


import tr.com.eaaslan.library.model.UserStatus;
import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.model.dto.user.UserUpdateRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "phoneNumber", source = "phoneNumber")
    User toEntity(UserCreateRequest userCreateRequest);

    @Mapping(target = "role", source = "role", qualifiedByName = "userRoleToString")
    @Mapping(target = "status", source = "status", qualifiedByName = "userStatusToString")
    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    void updateEntity(UserUpdateRequest updateRequest, @MappingTarget User user);

    @Named("userStatusToString")
    default String userStatusToString(UserStatus status) {
        if (status == null) {
            return null;
        }
        return status.name();
    }

    @Named("stringToUserRole")
    default UserRole stringToUserRole(String role) {
        if (role == null) {
            return null;
        }
        try {
            return UserRole.valueOf(role);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user role: " + role);
        }
    }

    @Named("userRoleToString")
    default String userRoleToString(UserRole role) {
        if (role == null) {
            return null;
        }
        return role.name();
    }
}