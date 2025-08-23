package com.carloscoral.r2dbc.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;
import java.util.Date;

public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    String id;
    String firstName;
    String lastName;
    Date birthday;
    String address;
    @Column(unique = true)
    String email;
    @Column(unique = true)
    String identification;
    String phone;
    @Column(name = "base_salary")
    BigDecimal baseSalary;
}
