package com.carloscoral.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request params for validate an existent user")
public class ValidateUserRequest {
    @Schema(description = "User's email address", example = "carlos.coral@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is not valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;
}
