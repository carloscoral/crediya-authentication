package com.carloscoral.api.common;

import com.carloscoral.api.dto.ValidationErrorResponse;
import com.carloscoral.api.exception.ValidationException;
import com.carloscoral.api.validation.GenericValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
@Slf4j
public abstract class BaseHandler {
    
    protected final GenericValidator validator;

    protected <T, U> Mono<ServerResponse> processWithValidationAndMapping(
            ServerRequest request,
            Class<T> dtoClass,
            Function<T, U> mapper,
            Function<U, Mono<Void>> processor,
            String successMessage) {
        
        return request.bodyToMono(dtoClass)
                .switchIfEmpty(Mono.error(new ValidationException("Request body is required", 
                        List.of("request: Request body cannot be empty"))))
                .doOnNext(dto -> log.debug("Request received: {}", dto))
                .flatMap(validator::validate)
                .map(mapper)
                .flatMap(processor)
                .then(ServerResponse.ok().bodyValue(successMessage))
                .onErrorResume(ValidationException.class, this::handleValidationError)
                .onErrorResume(this::handleGenericError);
    }


    protected <T> Mono<ServerResponse> processWithValidation(
            ServerRequest request,
            Class<T> dtoClass,
            Function<T, Mono<Void>> processor,
            String successMessage) {
        
        return processWithValidationAndMapping(request, dtoClass, Function.identity(), processor, successMessage);
    }

    protected Mono<ServerResponse> handleValidationError(ValidationException validationException) {
        log.warn("Validation error: {}", validationException.getValidationErrors());
        return ServerResponse.status(HttpStatus.BAD_REQUEST)
                .bodyValue(ValidationErrorResponse.builder()
                        .message("Validation error")
                        .errors(validationException.getValidationErrors())
                        .build());
    }

    protected Mono<ServerResponse> handleGenericError(Throwable error) {
        log.info("ERROR:", error);
        log.error("Error creating user: {}", error.getMessage(), error);
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .bodyValue("Internal server error: " + error.getMessage());
    }
}
