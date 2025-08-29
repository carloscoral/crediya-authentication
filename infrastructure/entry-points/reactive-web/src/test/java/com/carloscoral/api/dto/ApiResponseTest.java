package com.carloscoral.api.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
    }

    @Test
    void shouldCreateSuccessResponseWithData() {
        String message = "Operation completed successfully";
        String data = "test-data";

        ApiResponse<String> response = ApiResponse.success(message, data);

        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertEquals(data, response.getData());
        assertNull(response.getErrors());
    }

    @Test
    void shouldCreateSuccessResponseWithoutData() {
        String message = "Operation completed successfully";

        ApiResponse<Object> response = ApiResponse.success(message);

        assertTrue(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
        assertNull(response.getErrors());
    }

    @Test
    void shouldCreateErrorResponseWithErrors() {
        String message = "Validation failed";
        List<String> errors = Arrays.asList("Email is required", "Password too weak");

        ApiResponse<Object> response = ApiResponse.error(message, errors);

        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
        assertEquals(errors, response.getErrors());
    }

    @Test
    void shouldCreateErrorResponseWithoutErrors() {
        String message = "Internal server error";

        ApiResponse<Object> response = ApiResponse.error(message);

        assertFalse(response.isSuccess());
        assertEquals(message, response.getMessage());
        assertNull(response.getData());
        assertNull(response.getErrors());
    }

    @Test
    void shouldSerializeSuccessResponseToJsonCorrectly() throws JsonProcessingException {
        ApiResponse<String> response = ApiResponse.success("Success message", "test-data");

        String json = objectMapper.writeValueAsString(response);
        JsonNode jsonNode = objectMapper.readTree(json);

        assertTrue(jsonNode.get("success").asBoolean());
        assertEquals("Success message", jsonNode.get("message").asText());
        assertEquals("test-data", jsonNode.get("data").asText());
        assertFalse(jsonNode.has("errors"));
    }

    @Test
    void shouldSerializeErrorResponseToJsonCorrectly() throws JsonProcessingException {
        List<String> errors = Arrays.asList("Error 1", "Error 2");
        ApiResponse<Object> response = ApiResponse.error("Error message", errors);

        String json = objectMapper.writeValueAsString(response);
        JsonNode jsonNode = objectMapper.readTree(json);

        assertFalse(jsonNode.get("success").asBoolean());
        assertEquals("Error message", jsonNode.get("message").asText());
        assertFalse(jsonNode.has("data"));
        assertTrue(jsonNode.has("errors"));
        assertEquals(2, jsonNode.get("errors").size());
    }

    @Test
    void shouldOmitNullFieldsInJsonSerialization() throws JsonProcessingException {
        ApiResponse<Object> response = ApiResponse.success("Only message");

        String json = objectMapper.writeValueAsString(response);
        JsonNode jsonNode = objectMapper.readTree(json);

        assertTrue(jsonNode.has("success"));
        assertTrue(jsonNode.has("message"));
        assertFalse(jsonNode.has("data"));
        assertFalse(jsonNode.has("errors"));
    }

    @Test
    void shouldHandleComplexDataTypes() {
        Map<String, Object> complexData = Map.of(
                "id", 123,
                "name", "John Doe",
                "active", true,
                "balance", new BigDecimal("1000.50")
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success("User data", complexData);

        assertTrue(response.isSuccess());
        assertEquals("User data", response.getMessage());
        assertEquals(complexData, response.getData());
        assertNull(response.getErrors());
    }

    @Test
    void shouldHandleGenericTypes() {
        List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5);

        ApiResponse<List<Integer>> response = ApiResponse.success("Number list", numbers);

        assertTrue(response.isSuccess());
        assertEquals("Number list", response.getMessage());
        assertEquals(numbers, response.getData());
        assertNull(response.getErrors());
    }

    @Test
    void shouldSerializeComplexDataToJson() throws JsonProcessingException {
        Map<String, Object> userData = Map.of(
                "id", 1,
                "email", "test@example.com",
                "createdAt", LocalDate.of(2024, 1, 15)
        );

        ApiResponse<Map<String, Object>> response = ApiResponse.success("User created", userData);

        String json = objectMapper.writeValueAsString(response);
        JsonNode jsonNode = objectMapper.readTree(json);

        assertTrue(jsonNode.get("success").asBoolean());
        assertEquals("User created", jsonNode.get("message").asText());
        assertTrue(jsonNode.has("data"));
        assertEquals(1, jsonNode.get("data").get("id").asInt());
        assertEquals("test@example.com", jsonNode.get("data").get("email").asText());
    }

    @Test
    void shouldHandleEmptyErrorsList() {
        List<String> emptyErrors = Collections.emptyList();
        ApiResponse<Object> response = ApiResponse.error("Error with empty list", emptyErrors);

        assertFalse(response.isSuccess());
        assertEquals("Error with empty list", response.getMessage());
        assertNull(response.getData());
        assertEquals(emptyErrors, response.getErrors());
    }

    @Test
    void shouldSerializeEmptyErrorsListToJson() throws JsonProcessingException {
        List<String> emptyErrors = Collections.emptyList();
        ApiResponse<Object> response = ApiResponse.error("Error message", emptyErrors);

        String json = objectMapper.writeValueAsString(response);
        JsonNode jsonNode = objectMapper.readTree(json);

        assertFalse(jsonNode.get("success").asBoolean());
        assertEquals("Error message", jsonNode.get("message").asText());
        assertTrue(jsonNode.has("errors"));
        assertTrue(jsonNode.get("errors").isArray());
        assertEquals(0, jsonNode.get("errors").size());
    }

    @Test
    void shouldHandleNullDataCorrectly() {
        ApiResponse<String> response = ApiResponse.success("Success", null);

        assertTrue(response.isSuccess());
        assertEquals("Success", response.getMessage());
        assertNull(response.getData());
        assertNull(response.getErrors());
    }

    @Test
    void shouldDeserializeFromJson() throws JsonProcessingException {
        String json = """
                {
                    "success": true,
                    "message": "Operation successful",
                    "data": "test-value"
                }
                """;

        JsonNode jsonNode = objectMapper.readTree(json);

        assertTrue(jsonNode.get("success").asBoolean());
        assertEquals("Operation successful", jsonNode.get("message").asText());
        assertEquals("test-value", jsonNode.get("data").asText());
    }

    @Test
    void shouldCreateResponseWithNullMessage() {
        ApiResponse<String> response = ApiResponse.success(null, "data");

        assertTrue(response.isSuccess());
        assertNull(response.getMessage());
        assertEquals("data", response.getData());
        assertNull(response.getErrors());
    }

    @Test
    void shouldCreateErrorResponseWithNullMessage() {
        List<String> errors = Arrays.asList("Error 1");
        ApiResponse<Object> response = ApiResponse.error(null, errors);

        assertFalse(response.isSuccess());
        assertNull(response.getMessage());
        assertNull(response.getData());
        assertEquals(errors, response.getErrors());
    }

    @Test
    void shouldHandleDifferentDataTypes() {
        // Test with Boolean
        ApiResponse<Boolean> booleanResponse = ApiResponse.success("Boolean result", true);
        assertTrue(booleanResponse.getData());

        // Test with Number
        ApiResponse<Integer> numberResponse = ApiResponse.success("Number result", 42);
        assertEquals(42, numberResponse.getData());

        // Test with BigDecimal
        ApiResponse<BigDecimal> decimalResponse = ApiResponse.success("Decimal result", new BigDecimal("99.99"));
        assertEquals(new BigDecimal("99.99"), decimalResponse.getData());

        // Test with LocalDate
        LocalDate date = LocalDate.of(2024, 12, 25);
        ApiResponse<LocalDate> dateResponse = ApiResponse.success("Date result", date);
        assertEquals(date, dateResponse.getData());
    }

    @Test
    void shouldMaintainImmutabilityOfResponseFields() {
        String message = "Test message";
        String data = "Test data";
        List<String> errors = Arrays.asList("Error 1", "Error 2");

        ApiResponse<String> successResponse = ApiResponse.success(message, data);
        ApiResponse<Object> errorResponse = ApiResponse.error(message, errors);

        // Fields should be final and immutable
        assertTrue(successResponse.isSuccess());
        assertFalse(errorResponse.isSuccess());
        
        // Original lists should not affect the response
        List<String> mutableErrors = Arrays.asList("Error 1", "Error 2");
        ApiResponse<Object> response = ApiResponse.error("Test", mutableErrors);
        
        assertNotNull(response.getErrors());
        assertEquals(mutableErrors, response.getErrors());
    }

    @Test
    void shouldReturnCorrectTypedResponses() {
        ApiResponse<String> stringResponse = ApiResponse.success("Message", "string-data");
        ApiResponse<Integer> integerResponse = ApiResponse.success("Message", 123);
        ApiResponse<Object> objectResponse = ApiResponse.success("Message");
        ApiResponse<Object> errorResponse = ApiResponse.error("Error message");

        assertEquals(String.class, stringResponse.getData().getClass());
        assertEquals(Integer.class, integerResponse.getData().getClass());
        assertNull(objectResponse.getData());
        assertFalse(errorResponse.isSuccess());
    }
}
