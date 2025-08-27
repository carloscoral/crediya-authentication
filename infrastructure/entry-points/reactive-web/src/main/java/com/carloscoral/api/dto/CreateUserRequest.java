package com.carloscoral.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for creating a new user")
public class CreateUserRequest {

    @Schema(description = "User's first name", example = "Carlos", required = true)
    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "First name can only contain letters and spaces")
    private String firstName;

    @Schema(description = "User's last name", example = "Coral", required = true)
    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "Last name can only contain letters and spaces")
    private String lastName;

    @Schema(description = "User's birth date", example = "1990-05-15", required = true)
    @Past(message = "Birth date must be in the past")
    private LocalDate birthday;

    @Schema(description = "User's residential address", example = "Calle 123 #45-67, Bogotá", required = true)
    @Size(min = 10, max = 100, message = "Address must be between 10 and 100 characters")
    private String address;

    @Schema(description = "User's email address", example = "carlos.coral@example.com", required = true)
    @NotBlank(message = "Email is required")
    @Email(message = "Email format is not valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Schema(description = "User's identification number", example = "123456789", required = true)
    @NotBlank(message = "Identification is required")
    @Pattern(regexp = "^[0-9A-Z]{8,12}$", message = "Identification must have between 8 and 12 numeric digits or letters")
    private String identification;

    @Schema(description = "User's phone number with country code", example = "+573001234567", required = true)
    @Pattern(regexp = "^\\+?[0-9]{10,12}$", message = "Phone must have the format +xxxxxxxxxxxx")
    private String phone;

    @Schema(description = "User's base salary in local currency", example = "5000000.00", required = true)
    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be greater than 0")
    @DecimalMax(value = "15000000.00", message = "Base salary cannot exceed 15000000.00")
    private BigDecimal baseSalary;
}
