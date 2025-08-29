package com.carloscoral.api.mapper;

import com.carloscoral.api.dto.CreateUserRequest;
import com.carloscoral.model.user.User;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    @Test
    void shouldMapCreateUserRequestToUser() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        User user = userMapper.toUser(request);

        assertNotNull(user);
        assertEquals(request.getFirstName(), user.getFirstName());
        assertEquals(request.getLastName(), user.getLastName());
        assertEquals(request.getBirthday(), user.getBirthday());
        assertEquals(request.getAddress(), user.getAddress());
        assertEquals(request.getEmail(), user.getEmail());
        assertEquals(request.getIdentification(), user.getIdentification());
        assertEquals(request.getPhone(), user.getPhone());
        assertEquals(request.getBaseSalary(), user.getBaseSalary());
    }

    @Test
    void shouldMapCreateUserRequestToUserWithNullValues() {
        CreateUserRequest request = CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        User user = userMapper.toUser(request);

        assertNotNull(user);
        assertEquals("Carlos", user.getFirstName());
        assertEquals("Coral", user.getLastName());
        assertEquals(LocalDate.of(1990, 5, 15), user.getBirthday());
        assertEquals("Calle 123 #45-67, Bogotá", user.getAddress());
        assertEquals("carlos.coral@example.com", user.getEmail());
        assertEquals("123456789", user.getIdentification());
        assertEquals("+573001234567", user.getPhone());
        assertEquals(new BigDecimal("5000000"), user.getBaseSalary());
    }

    @Test
    void shouldHandleNullRequest() {
        CreateUserRequest request = null;

        User user = userMapper.toUser(request);

        assertNull(user);
    }
}
