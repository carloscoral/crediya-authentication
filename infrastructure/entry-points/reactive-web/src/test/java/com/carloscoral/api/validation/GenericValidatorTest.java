package com.carloscoral.api.validation;

import com.carloscoral.api.dto.CreateUserRequest;
import com.carloscoral.api.exception.ValidationException;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class GenericValidatorTest {

    private GenericValidator genericValidator;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        genericValidator = new GenericValidator(validator);
    }

    @Test
    void shouldValidateValidRequest() {
        CreateUserRequest validRequest = CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        StepVerifier.create(genericValidator.validate(validRequest))
                .expectNext(validRequest)
                .verifyComplete();
    }

    @Test
    void shouldFailValidationForInvalidEmail() {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("invalid-email") // Invalid email
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        StepVerifier.create(genericValidator.validate(invalidRequest))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void shouldFailValidationForEmptyFirstName() {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("") // Empty name
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        StepVerifier.create(genericValidator.validate(invalidRequest))
                .expectError(ValidationException.class)
                .verify();
    }

    @Test
    void shouldFailValidationForNegativeSalary() {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("-1000")) // Negative salary
                .build();

        StepVerifier.create(genericValidator.validate(invalidRequest))
                .expectError(ValidationException.class)
                .verify();
    }
}
