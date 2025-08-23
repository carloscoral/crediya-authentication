package com.carloscoral.model.user;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Date;

@Getter
@Setter
@AllArgsConstructor
@Builder(toBuilder = true)
public class User {
    String firstName;
    String lastName;
    Date birthday;
    String address;
    String email;
    String identification;
    String phone;
    BigDecimal baseSalary;
}
