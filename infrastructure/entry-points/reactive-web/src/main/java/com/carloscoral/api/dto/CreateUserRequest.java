package com.carloscoral.api.dto;

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
public class CreateUserRequest {

    @NotBlank(message = "First name is required")
    @Size(min = 2, max = 50, message = "First name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "First name can only contain letters and spaces")
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters")
    @Pattern(regexp = "^[a-zA-ZÀ-ÿ\\s]+$", message = "Last name can only contain letters and spaces")
    private String lastName;

    @Past(message = "Birth date must be in the past")
    private LocalDate birthday;

    @Size(min = 10, max = 100, message = "Address must be between 10 and 100 characters")
    private String address;

    @NotBlank(message = "Email is required")
    @Email(message = "Email format is not valid")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @NotBlank(message = "Identification is required")
    @Pattern(regexp = "^[0-9A-Z]{8,12}$", message = "Identification must have between 8 and 12 numeric digits or letters")
    private String identification;

    @Pattern(regexp = "^\\+?[0-9]{10,12}$", message = "Phone must have the format +xxxxxxxxxxxx")
    private String phone;

    @NotNull(message = "Base salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Base salary must be greater than 0")
    @DecimalMax(value = "999999999.99", message = "Base salary cannot exceed 999,999,999.99")
    private BigDecimal baseSalary;
}
