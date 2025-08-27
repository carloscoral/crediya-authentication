package com.carloscoral.api;

import com.carloscoral.api.dto.CreateUserRequest;
import com.carloscoral.api.exception.GlobalExceptionHandler;
import com.carloscoral.api.exception.ValidationException;
import com.carloscoral.usecase.exception.DuplicateUserException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    private UserController userController;
    private WebTestClient webTestClient;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userController = new UserController(userService);
        
        webTestClient = WebTestClient.bindToController(userController)
                .controllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
    }

    @Test
    void shouldCreateUserSuccessfully() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.just("User created successfully"));

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

        String requestBody = objectMapper.writeValueAsString(validRequest);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.success").isEqualTo(true)
                .jsonPath("$.message").isEqualTo("User created successfully")
                .jsonPath("$.data").doesNotExist()
                .jsonPath("$.errors").doesNotExist();
    }

    @Test
    void shouldReturnConflictForDuplicateUser() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.error(new DuplicateUserException()));

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

        String requestBody = objectMapper.writeValueAsString(validRequest);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("User already exists")
                .jsonPath("$.data").doesNotExist()
                .jsonPath("$.errors").doesNotExist();
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("invalid-email")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.error(new ValidationException("Validation error", 
                        List.of("email: Email format is not valid"))));

        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Validation failed")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").isEqualTo("email: Email format is not valid")
                .jsonPath("$.data").doesNotExist();
    }

    @Test
    void shouldReturnBadRequestForEmptyFirstName() throws Exception {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.error(new ValidationException("Validation error", 
                        List.of("firstName: First name is required"))));

        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Validation failed")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").isEqualTo("firstName: First name is required")
                .jsonPath("$.data").doesNotExist();
    }

    @Test
    void shouldReturnBadRequestForNullBaseSalary() throws Exception {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(null)
                .build();

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.error(new ValidationException("Validation error", 
                        List.of("baseSalary: Base salary is required"))));

        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Validation failed")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").isEqualTo("baseSalary: Base salary is required")
                .jsonPath("$.data").doesNotExist();
    }

    @Test
    void shouldReturnBadRequestForInvalidPhone() throws Exception {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("invalid-phone")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.error(new ValidationException("Validation error", 
                        List.of("phone: Phone must have the format +xxxxxxxxxxxx"))));

        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Validation failed")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").isEqualTo("phone: Phone must have the format +xxxxxxxxxxxx")
                .jsonPath("$.data").doesNotExist();
    }

    @Test
    void shouldReturnInternalServerErrorForUnexpectedError() throws Exception {
        when(userService.createUser(any(CreateUserRequest.class)))
                .thenReturn(Mono.error(new RuntimeException("Unexpected error")));

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

        String requestBody = objectMapper.writeValueAsString(validRequest);

        webTestClient.post()
                .uri("/api/v1/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.success").isEqualTo(false)
                .jsonPath("$.message").isEqualTo("Internal server error occurred")
                .jsonPath("$.data").doesNotExist()
                .jsonPath("$.errors").doesNotExist();
    }
}
