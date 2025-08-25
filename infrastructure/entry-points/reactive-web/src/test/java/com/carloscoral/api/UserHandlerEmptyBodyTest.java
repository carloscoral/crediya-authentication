package com.carloscoral.api;

import com.carloscoral.api.mapper.UserMapper;
import com.carloscoral.api.validation.GenericValidator;
import com.carloscoral.usecase.createuser.CreateUserUseCase;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;

@ExtendWith(MockitoExtension.class)
class UserHandlerEmptyBodyTest {

    @Mock
    private CreateUserUseCase createUserUseCase;
    
    @Mock
    private UserMapper userMapper;

    private UserHandler userHandler;
    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        GenericValidator genericValidator = new GenericValidator(validator);
        
        userHandler = new UserHandler(genericValidator, createUserUseCase, userMapper);
        
        RouterFunction<ServerResponse> routes = RouterFunctions.route(
                POST("/users"), userHandler::listenPOSTCreateUser);
        
        webTestClient = WebTestClient.bindToRouterFunction(routes).build();
    }

    @Test
    void shouldReturnValidationErrorForNoBody() {
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.message").isEqualTo("Validation error")
                .jsonPath("$.errors").isArray()
                .jsonPath("$.errors.length()").isEqualTo(1)
                .jsonPath("$.errors[0]").isEqualTo("request: Request body cannot be empty");
    }

    @Test
    void shouldReturnBadRequestForMalformedJson() {
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{invalid json}")
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturnBadRequestForInvalidContentType() {
        webTestClient.post()
                .uri("/users")
                .contentType(MediaType.TEXT_PLAIN)
                .bodyValue("some text")
                .exchange()
                .expectStatus().isEqualTo(415); // Unsupported Media Type
    }
}
