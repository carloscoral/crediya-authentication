package com.carloscoral.usecase.validateuser;

import com.carloscoral.model.user.User;
import com.carloscoral.model.user.gateways.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ValidateUserUseCaseTest {

    @InjectMocks
    private ValidateUserUseCase validateUserUseCase;

    @Mock
    private UserRepository userRepository;

    private User existingUser;
    private String validEmail;
    private String nonExistentEmail;

    @BeforeEach
    void setUp() {
        validEmail = "carlos.coral@example.com";
        nonExistentEmail = "nonexistent@example.com";

        existingUser = User.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email(validEmail)
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();
    }

    @Test
    void shouldReturnTrueWhenUserExists() {
        when(userRepository.findByEmail(validEmail))
                .thenReturn(Mono.just(existingUser));

        StepVerifier.create(validateUserUseCase.byEmail(validEmail))
                .expectNext(true)
                .verifyComplete();

        verify(userRepository).findByEmail(validEmail);
    }

    @Test
    void shouldReturnFalseWhenUserDoesNotExist() {
        when(userRepository.findByEmail(nonExistentEmail))
                .thenReturn(Mono.empty());

        StepVerifier.create(validateUserUseCase.byEmail(nonExistentEmail))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository).findByEmail(nonExistentEmail);
    }

    @Test
    void shouldHandleRepositoryError() {
        RuntimeException repositoryException = new RuntimeException("Database connection error");
        when(userRepository.findByEmail(validEmail))
                .thenReturn(Mono.error(repositoryException));

        StepVerifier.create(validateUserUseCase.byEmail(validEmail))
                .expectError(RuntimeException.class)
                .verify();

        verify(userRepository).findByEmail(validEmail);
    }

    @Test
    void shouldHandleNullEmail() {
        when(userRepository.findByEmail(null))
                .thenReturn(Mono.empty());

        StepVerifier.create(validateUserUseCase.byEmail(null))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository).findByEmail(null);
    }

    @Test
    void shouldHandleEmptyEmail() {
        String emptyEmail = "";
        when(userRepository.findByEmail(emptyEmail))
                .thenReturn(Mono.empty());

        StepVerifier.create(validateUserUseCase.byEmail(emptyEmail))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository).findByEmail(emptyEmail);
    }

    @Test
    void shouldHandleBlankEmail() {
        String blankEmail = "   ";
        when(userRepository.findByEmail(blankEmail))
                .thenReturn(Mono.empty());

        StepVerifier.create(validateUserUseCase.byEmail(blankEmail))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository).findByEmail(blankEmail);
    }

    @Test
    void shouldCreateInstanceWithCorrectDependencies() {
        ValidateUserUseCase useCase = new ValidateUserUseCase(userRepository);

        assertNotNull(useCase);
    }

    @Test
    void shouldHandleDifferentValidEmails() {
        String[] validEmails = {
                "test@example.com",
                "user.name@domain.co",
                "email+tag@test.org",
                "number123@email.net"
        };

        for (String email : validEmails) {
            User testUser = User.builder()
                    .firstName("Test")
                    .lastName("User")
                    .email(email)
                    .identification("123")
                    .birthday(LocalDate.of(1990, 1, 1))
                    .address("Test Address")
                    .phone("+1234567890")
                    .baseSalary(new BigDecimal("1000"))
                    .build();

            when(userRepository.findByEmail(email))
                    .thenReturn(Mono.just(testUser));

            StepVerifier.create(validateUserUseCase.byEmail(email))
                    .expectNext(true)
                    .verifyComplete();

            verify(userRepository).findByEmail(email);
        }
    }

    @Test
    void shouldPropagateRepositoryExceptionsCorrectly() {
        IllegalArgumentException illegalArgException = new IllegalArgumentException("Invalid email format");
        when(userRepository.findByEmail(validEmail))
                .thenReturn(Mono.error(illegalArgException));

        StepVerifier.create(validateUserUseCase.byEmail(validEmail))
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    "Invalid email format".equals(throwable.getMessage()))
                .verify();

        verify(userRepository).findByEmail(validEmail);
    }

    @Test
    void shouldHandleTimeoutException() {
        when(userRepository.findByEmail(validEmail))
                .thenReturn(Mono.error(new RuntimeException("Timeout")));

        StepVerifier.create(validateUserUseCase.byEmail(validEmail))
                .expectErrorMatches(throwable -> 
                    throwable instanceof RuntimeException &&
                    "Timeout".equals(throwable.getMessage()))
                .verify();

        verify(userRepository).findByEmail(validEmail);
    }

    @Test
    void shouldCallRepositoryOnlyOnce() {
        when(userRepository.findByEmail(validEmail))
                .thenReturn(Mono.just(existingUser));

        StepVerifier.create(validateUserUseCase.byEmail(validEmail))
                .expectNext(true)
                .verifyComplete();

        verify(userRepository, times(1)).findByEmail(validEmail);
        verifyNoMoreInteractions(userRepository);
    }

    @Test
    void shouldHandleRepositoryReturningNullUser() {
        when(userRepository.findByEmail(validEmail))
                .thenReturn(Mono.justOrEmpty(null));

        StepVerifier.create(validateUserUseCase.byEmail(validEmail))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository).findByEmail(validEmail);
    }

    @Test
    void shouldValidateEmailCaseSensitivity() {
        String lowercaseEmail = "test@example.com";
        String uppercaseEmail = "TEST@EXAMPLE.COM";
        String mixedCaseEmail = "Test@Example.Com";

        when(userRepository.findByEmail(lowercaseEmail))
                .thenReturn(Mono.just(existingUser));

        StepVerifier.create(validateUserUseCase.byEmail(lowercaseEmail))
                .expectNext(true)
                .verifyComplete();

        when(userRepository.findByEmail(uppercaseEmail))
                .thenReturn(Mono.empty());

        StepVerifier.create(validateUserUseCase.byEmail(uppercaseEmail))
                .expectNext(false)
                .verifyComplete();

        when(userRepository.findByEmail(mixedCaseEmail))
                .thenReturn(Mono.empty());

        StepVerifier.create(validateUserUseCase.byEmail(mixedCaseEmail))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository).findByEmail(lowercaseEmail);
        verify(userRepository).findByEmail(uppercaseEmail);
        verify(userRepository).findByEmail(mixedCaseEmail);
    }

    @Test
    void shouldHandleSpecialCharactersInEmail() {
        String specialEmail = "user+test@domain-name.co.uk";
        when(userRepository.findByEmail(specialEmail))
                .thenReturn(Mono.just(existingUser));

        StepVerifier.create(validateUserUseCase.byEmail(specialEmail))
                .expectNext(true)
                .verifyComplete();

        verify(userRepository).findByEmail(specialEmail);
    }

    @Test
    void shouldHandleVeryLongEmail() {
        String longEmail = "a".repeat(50) + "@" + "domain".repeat(10) + ".com";
        when(userRepository.findByEmail(longEmail))
                .thenReturn(Mono.empty());

        StepVerifier.create(validateUserUseCase.byEmail(longEmail))
                .expectNext(false)
                .verifyComplete();

        verify(userRepository).findByEmail(longEmail);
    }
}
