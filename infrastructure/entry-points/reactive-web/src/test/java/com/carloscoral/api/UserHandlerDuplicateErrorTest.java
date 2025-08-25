package com.carloscoral.api;

import com.carloscoral.api.dto.CreateUserRequest;
import com.carloscoral.api.mapper.UserMapper;
import com.carloscoral.api.validation.GenericValidator;
import com.carloscoral.model.user.User;
import com.carloscoral.usecase.createuser.CreateUserUseCase;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

/**
 * Tests for duplicate resource error handling in UserHandler
 */
@ExtendWith(MockitoExtension.class)
class UserHandlerDuplicateErrorTest {

    @Mock
    private CreateUserUseCase createUserUseCase;
    
    @Mock
    private UserMapper userMapper;

    private UserHandler userHandler;
    private WebTestClient webTestClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        GenericValidator genericValidator = new GenericValidator(validator);
        
        when(userMapper.toUser(any(CreateUserRequest.class))).thenReturn(
                User.builder()
                        .firstName("Carlos")
                        .lastName("Coral")
                        .birthday(LocalDate.of(1990, 5, 15))
                        .address("Calle 123 #45-67, Bogotá")
                        .email("carlos.coral@example.com")
                        .identification("123456789")
                        .phone("+573001234567")
                        .baseSalary(new BigDecimal("5000000"))
                        .build()
        );
        
        userHandler = new UserHandler(genericValidator, createUserUseCase, userMapper);
        
        RouterFunction<ServerResponse> routes = RouterFunctions.route(
                POST("/users"), userHandler::listenPOSTCreateUser);
        
        webTestClient = WebTestClient.bindToRouterFunction(routes).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void shouldHandleDatabaseConstraintViolationForEmail() throws Exception {
        RuntimeException dbError = new RuntimeException("duplicate key value violates unique constraint \"users_email_key\"");
        when(createUserUseCase.execute(any(User.class))).thenReturn(Mono.error(dbError));

        CreateUserRequest validRequest = getValidUserRequest();
        String requestBody = objectMapper.writeValueAsString(validRequest);

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isEqualTo(409) // Conflict
                .expectBody()
                .jsonPath("$.message").isEqualTo("Duplicate resource")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("Email address"));
    }

    @Test
    void shouldHandleDatabaseConstraintViolationForIdentification() throws Exception {
        RuntimeException dbError = new RuntimeException("duplicate key value violates unique constraint \"users_identification_key\"");
        when(createUserUseCase.execute(any(User.class))).thenReturn(Mono.error(dbError));

        CreateUserRequest validRequest = getValidUserRequest();
        String requestBody = objectMapper.writeValueAsString(validRequest);

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isEqualTo(409) // Conflict
                .expectBody()
                .jsonPath("$.message").isEqualTo("Duplicate resource")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").value(org.hamcrest.Matchers.containsString("Identification number"));
    }

    @Test
    void shouldHandleGenericDatabaseConstraintViolation() throws Exception {
        RuntimeException dbError = new RuntimeException("Some generic constraint violation");
        when(createUserUseCase.execute(any(User.class))).thenReturn(Mono.error(dbError));

        CreateUserRequest validRequest = getValidUserRequest();
        String requestBody = objectMapper.writeValueAsString(validRequest);

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Database constraint violation")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").isEqualTo("The provided data violates database constraints");
    }

    private CreateUserRequest getValidUserRequest() {
        return CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();
    }
}
