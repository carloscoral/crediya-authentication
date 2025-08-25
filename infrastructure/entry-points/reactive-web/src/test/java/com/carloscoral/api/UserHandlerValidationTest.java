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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@ExtendWith(MockitoExtension.class)
class UserHandlerValidationTest {

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
    void shouldCreateUserSuccessfully() throws Exception {
        when(createUserUseCase.execute(any(User.class))).thenReturn(Mono.empty());

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
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .isEqualTo("User created successfully");
    }

    @Test
    void shouldReturnBadRequestForInvalidEmail() throws Exception {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("invalid-email") // Email inválido
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForEmptyFirstName() throws Exception {
        CreateUserRequest invalidRequest = CreateUserRequest.builder()
                .firstName("") // Nombre vacío
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest();
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
                .phone("123456") // Teléfono inválido
                .baseSalary(new BigDecimal("5000000"))
                .build();

        String requestBody = objectMapper.writeValueAsString(invalidRequest);

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(requestBody)
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForEmptyBody() {
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Error de validación")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").value(containsString("request body"));
    }

    @Test
    void shouldReturnBadRequestForEmptyJsonObject() throws Exception {
        String emptyJson = "{}";

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(emptyJson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Error de validación")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").value(containsString("request body"));
    }

    @Test
    void shouldReturnBadRequestForNullFieldsObject() throws Exception {
        String nullFieldsJson = "{\"firstName\": null, \"lastName\": null, \"email\": null}";

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(nullFieldsJson)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Error de validación")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors[0]").value(containsString("request body"));
    }

    @Test
    void shouldReturnBadRequestForInvalidJson() {
        String invalidJson = "{invalid json";

        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(invalidJson)
                .exchange()
                .expectStatus().isBadRequest();
    }
}
