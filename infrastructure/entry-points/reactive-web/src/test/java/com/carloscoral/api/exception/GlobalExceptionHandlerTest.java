package com.carloscoral.api.exception;

import com.carloscoral.api.dto.ApiResponse;
import com.carloscoral.usecase.exception.DuplicateUserException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.codec.DecodingException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ServerWebInputException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    private ValidationException validationException;
    private DuplicateUserException duplicateUserException;
    private JsonParseException jsonParseException;
    private JsonProcessingException jsonProcessingException;
    private DecodingException decodingException;
    private ServerWebInputException serverWebInputException;

    @BeforeEach
    void setUp() {
        validationException = new ValidationException(
                "Validation failed", 
                List.of("email: Email format is not valid", "firstName: First name is required")
        );
        
        duplicateUserException = new DuplicateUserException();
        
        jsonParseException = new JsonParseException(null, "Unexpected character");
        
        jsonProcessingException = new JsonProcessingException("Invalid JSON") {};
        
        decodingException = new DecodingException("Failed to decode");
        
        serverWebInputException = new ServerWebInputException("Failed to read HTTP message", null, jsonProcessingException);
    }

    @Test
    void shouldHandleValidationException() {
        Mono<ResponseEntity<ApiResponse<List<String>>>> result = 
                globalExceptionHandler.handleValidationException(validationException);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    
                    ApiResponse<List<String>> body = response.getBody();
                    assertNotNull(body);
                    assertFalse(body.isSuccess());
                    assertEquals("Validation failed", body.getMessage());
                    assertEquals(2, body.getErrors().size());
                    assertTrue(body.getErrors().contains("email: Email format is not valid"));
                    assertTrue(body.getErrors().contains("firstName: First name is required"));
                    assertNull(body.getData());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleDuplicateUserException() {
        Mono<ResponseEntity<ApiResponse<Object>>> result = 
                globalExceptionHandler.handleDuplicateUserException(duplicateUserException);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    
                    ApiResponse<Object> body = response.getBody();
                    assertNotNull(body);
                    assertFalse(body.isSuccess());
                    assertEquals("User already exists", body.getMessage());
                    assertNull(body.getData());
                    assertNull(body.getErrors());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleJsonParseException() {
        Mono<ResponseEntity<ApiResponse<Object>>> result = 
                globalExceptionHandler.handleJsonParsingException(jsonParseException);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    
                    ApiResponse<Object> body = response.getBody();
                    assertNotNull(body);
                    assertFalse(body.isSuccess());
                    assertEquals("Invalid JSON format in request body", body.getMessage());
                    assertNull(body.getData());
                    assertNull(body.getErrors());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleJsonProcessingException() {
        Mono<ResponseEntity<ApiResponse<Object>>> result = 
                globalExceptionHandler.handleJsonParsingException(jsonProcessingException);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    
                    ApiResponse<Object> body = response.getBody();
                    assertNotNull(body);
                    assertFalse(body.isSuccess());
                    assertEquals("Invalid JSON format in request body", body.getMessage());
                    assertNull(body.getData());
                    assertNull(body.getErrors());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleDecodingException() {
        Mono<ResponseEntity<ApiResponse<Object>>> result = 
                globalExceptionHandler.handleJsonParsingException(decodingException);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    
                    ApiResponse<Object> body = response.getBody();
                    assertNotNull(body);
                    assertFalse(body.isSuccess());
                    assertEquals("Invalid JSON format in request body", body.getMessage());
                    assertNull(body.getData());
                    assertNull(body.getErrors());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleServerWebInputExceptionWithJsonCause() {
        Mono<ResponseEntity<ApiResponse<Object>>> result = 
                globalExceptionHandler.handleServerWebInputException(serverWebInputException);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    
                    ApiResponse<Object> body = response.getBody();
                    assertNotNull(body);
                    assertFalse(body.isSuccess());
                    assertEquals("Invalid JSON format in request body", body.getMessage());
                    assertNull(body.getData());
                    assertNull(body.getErrors());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleServerWebInputExceptionWithoutJsonCause() {
        ServerWebInputException nonJsonException = new ServerWebInputException(
                "Failed to read HTTP message", null, new RuntimeException("Some other error"));

        Mono<ResponseEntity<ApiResponse<Object>>> result = 
                globalExceptionHandler.handleServerWebInputException(nonJsonException);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    
                    ApiResponse<Object> body = response.getBody();
                    assertNotNull(body);
                    assertFalse(body.isSuccess());
                    assertEquals("Invalid request format", body.getMessage());
                    assertNull(body.getData());
                    assertNull(body.getErrors());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleGenericException() {
        RuntimeException genericException = new RuntimeException("Database connection failed");

        Mono<ResponseEntity<ApiResponse<Object>>> result = 
                globalExceptionHandler.handleGenericException(genericException);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    
                    ApiResponse<Object> body = response.getBody();
                    assertNotNull(body);
                    assertFalse(body.isSuccess());
                    assertEquals("Internal server error occurred", body.getMessage());
                    assertNull(body.getData());
                    assertNull(body.getErrors());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleServerWebInputExceptionWithNestedJsonCause() {
        // Test with nested exception hierarchy
        RuntimeException intermediate = new RuntimeException("Intermediate exception", jsonProcessingException);
        ServerWebInputException nestedJsonException = new ServerWebInputException(
                "Failed to read HTTP message", null, intermediate);

        Mono<ResponseEntity<ApiResponse<Object>>> result = 
                globalExceptionHandler.handleServerWebInputException(nestedJsonException);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    
                    ApiResponse<Object> body = response.getBody();
                    assertNotNull(body);
                    assertFalse(body.isSuccess());
                    assertEquals("Invalid JSON format in request body", body.getMessage());
                    assertNull(body.getData());
                    assertNull(body.getErrors());
                })
                .verifyComplete();
    }

    @Test
    void shouldHandleValidationExceptionWithEmptyErrors() {
        ValidationException emptyErrorsException = new ValidationException("Validation failed", List.of());

        Mono<ResponseEntity<ApiResponse<List<String>>>> result = 
                globalExceptionHandler.handleValidationException(emptyErrorsException);

        StepVerifier.create(result)
                .assertNext(response -> {
                    assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
                    assertEquals(MediaType.APPLICATION_JSON, response.getHeaders().getContentType());
                    
                    ApiResponse<List<String>> body = response.getBody();
                    assertNotNull(body);
                    assertFalse(body.isSuccess());
                    assertEquals("Validation failed", body.getMessage());
                    assertTrue(body.getErrors().isEmpty());
                    assertNull(body.getData());
                })
                .verifyComplete();
    }
}
