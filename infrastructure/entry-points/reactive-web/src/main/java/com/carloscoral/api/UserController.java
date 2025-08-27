package com.carloscoral.api;

import com.carloscoral.api.dto.ApiResponse;
import com.carloscoral.api.dto.CreateUserRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    private final UserService userService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<Object>>> createUser(@RequestBody CreateUserRequest request) {
        log.debug("Received create user request: {}", request);

        return userService.createUser(request)
                .map(message -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(ApiResponse.success(message)));
    }
}
