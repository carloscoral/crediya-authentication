package com.carloscoral.api;

import com.carloscoral.api.dto.CreateUserRequest;
import com.carloscoral.api.exception.ValidationException;
import com.carloscoral.api.mapper.UserMapper;
import com.carloscoral.api.validation.GenericValidator;
import com.carloscoral.usecase.createuser.CreateUserUseCase;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    
    private final CreateUserUseCase createUserUseCase;
    private final UserMapper userMapper;
    private final GenericValidator validator;

    public Mono<String> createUser(CreateUserRequest request) {
        if (request == null) {
            return Mono.error(new ValidationException("Request body is required", 
                    List.of("request: Request body cannot be empty")));
        }
        
        return Mono.just(request)
                .doOnNext(dto -> log.debug("Processing create user request: {}", dto))
                .flatMap(validator::validate)
                .map(userMapper::toUser)
                .flatMap(createUserUseCase::execute)
                .then(Mono.just("User created successfully"));
    }
}
