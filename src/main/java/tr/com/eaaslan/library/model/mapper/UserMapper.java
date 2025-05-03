package tr.com.eaaslan.library.model.mapper;

import org.mapstruct.*;
import tr.com.eaaslan.library.model.User;
import tr.com.eaaslan.library.model.UserRole;


import tr.com.eaaslan.library.model.dto.user.UserCreateRequest;
import tr.com.eaaslan.library.model.dto.user.UserResponse;
import tr.com.eaaslan.library.model.dto.user.UserUpdateRequest;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface UserMapper {

    @Mapping(target = "role", source = "role", qualifiedByName = "stringToUserRole")
    User toEntity(UserCreateRequest userCreateRequest);

    @Mapping(target = "role", source = "role", qualifiedByName = "userRoleToString")
    UserResponse toResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "role", expression = "java(updateRequest.role() != null ? stringToUserRole(updateRequest.role()) : user.getRole())")
    void updateEntity(UserUpdateRequest updateRequest, @MappingTarget User user);

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