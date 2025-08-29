package com.carloscoral.model.user;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    String firstName;
    String lastName;
    LocalDate birthday;
    String address;
    String email;
    String identification;
    String phone;
    BigDecimal baseSalary;
}
