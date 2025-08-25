package com.carloscoral.usecase.exception;

public class DuplicateUserException extends RuntimeException {
    public DuplicateUserException() {
        super("User already exists");
    }
}
