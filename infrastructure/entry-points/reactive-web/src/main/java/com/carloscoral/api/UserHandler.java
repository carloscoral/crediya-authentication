package com.carloscoral.api;

import com.carloscoral.api.common.BaseHandler;
import com.carloscoral.api.dto.CreateUserRequest;
import com.carloscoral.api.exception.ValidationException;
import com.carloscoral.api.mapper.UserMapper;
import com.carloscoral.api.validation.GenericValidator;
import com.carloscoral.usecase.createuser.CreateUserUseCase;
import com.carloscoral.usecase.exception.DuplicateUserException;

import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class UserHandler extends BaseHandler {
    
    private final CreateUserUseCase createUserUseCase;
    private final UserMapper userMapper;

    public UserHandler(GenericValidator validator, CreateUserUseCase createUserUseCase, UserMapper userMapper) {
        super(validator);
        this.createUserUseCase = createUserUseCase;
        this.userMapper = userMapper;
    }

    public Mono<ServerResponse> listenPOSTCreateUser(ServerRequest serverRequest) {
        return serverRequest.bodyToMono(CreateUserRequest.class)
                .switchIfEmpty(Mono.error(new ValidationException("Request body is required", 
                        List.of("request: Request body cannot be empty"))))
                .doOnNext(dto -> log.debug("Request received: {}", dto))
                .flatMap(validator::validate)
                .map(userMapper::toUser)
                .flatMap(createUserUseCase::execute)
                .then(ServerResponse.ok().bodyValue("User created successfully"))
                .onErrorResume(ValidationException.class, this::handleValidationError)
                .onErrorResume(DuplicateUserException.class, this::handleDuplicateUserError)
                .onErrorResume(this::handleGenericError);
    }

    private Mono<ServerResponse> handleDuplicateUserError(DuplicateUserException exception) {
        return ServerResponse.status(HttpStatus.CONFLICT)
                .bodyValue(exception.getMessage());
    }
}