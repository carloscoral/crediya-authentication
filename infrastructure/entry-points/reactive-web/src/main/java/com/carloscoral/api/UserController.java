package com.carloscoral.api;

import com.carloscoral.api.dto.ApiResponse;
import com.carloscoral.api.dto.CreateUserRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Users", description = "User management operations")
public class UserController {

    private final UserService userService;

    @PostMapping
    @Operation(
            summary = "Create a new user",
            description = "Creates a new user in the system.",
            operationId = "createUser"
    )
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "201",
                description = "User created successfully",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiResponse.class),
                        examples = @ExampleObject(
                                name = "Success Response",
                                value = """
                                        {
                                        "success": true,
                                        "message": "User created successfully"
                                        }"""
                        )
                )
        )
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "400",
                description = "Validation errors in request body",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiResponse.class),
                        examples = @ExampleObject(
                                name = "Validation Error",
                                value = """
                                        {
                                        "success": false,
                                        "message": "Validation failed",
                                        "errors": [
                                        "Email is required",
                                        "First name must be between 2 and 50 characters"
                                        ]
                                        }"""
                        )
                )
        )
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "409",
                description = "User already exists with provided email or identification",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiResponse.class),
                        examples = @ExampleObject(
                                name = "Duplicate User",
                                value = """
                                        {
                                        "success": false,
                                        "message": "User already exists"
                                        }"""
                        )
                )
        )
        @io.swagger.v3.oas.annotations.responses.ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = ApiResponse.class),
                        examples = @ExampleObject(
                                name = "Server Error",
                                value = """
                                        {
                                        "success": false,
                                        "message": "Internal server error occurred"
                                        }"""
                        )
                )
        )
    @RequestBody(
            description = "User information to create the account",
            required = true,
            content = @Content(
                    mediaType = MediaType.APPLICATION_JSON_VALUE,
                    schema = @Schema(implementation = CreateUserRequest.class),
                    examples = @ExampleObject(
                            name = "Valid User Request",
                            value = """
                                    {
                                      "firstName": "Carlos",
                                      "lastName": "Coral",
                                      "birthday": "1990-05-15",
                                      "address": "Calle 123 #45-67, Bogotá",
                                      "email": "carlos.coral@example.com",
                                      "identification": "123456789",
                                      "phone": "+573001234567",
                                      "baseSalary": 5000000.00
                                    }"""
                    )
            )
    )
    public Mono<ResponseEntity<ApiResponse<Object>>> createUser(@org.springframework.web.bind.annotation.RequestBody CreateUserRequest request) {
        log.debug("Received create user request: {}", request);

        return userService.createUser(request)
                .map(message -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(message)));
    }
}
