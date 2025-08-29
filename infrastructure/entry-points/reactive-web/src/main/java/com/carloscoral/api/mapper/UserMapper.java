package com.carloscoral.api.mapper;

import com.carloscoral.api.dto.CreateUserRequest;
import com.carloscoral.model.user.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {
    User toUser(CreateUserRequest request);
}
