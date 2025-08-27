package com.carloscoral.api.exception;

import com.carloscoral.api.dto.ApiResponse;
import com.carloscoral.usecase.exception.DuplicateUserException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ValidationException.class)
    public Mono<ResponseEntity<ApiResponse<List<String>>>> handleValidationException(ValidationException validationException) {
        log.warn("Validation error: {}", validationException.getValidationErrors());
        
        ApiResponse<List<String>> errorResponse = ApiResponse.error("Validation failed", validationException.getValidationErrors());
        
        return Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }

    @ExceptionHandler(DuplicateUserException.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleDuplicateUserException(DuplicateUserException exception) {
        log.warn("Duplicate user error: {}", exception.getMessage());
        
        ApiResponse<Object> errorResponse = ApiResponse.error(exception.getMessage());
        
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ApiResponse<Object>>> handleGenericException(Exception error) {
        log.error("Unexpected error: {}", error.getMessage(), error);
        
        ApiResponse<Object> errorResponse = ApiResponse.error("Internal server error occurred");
        
        return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .body(errorResponse));
    }
}
