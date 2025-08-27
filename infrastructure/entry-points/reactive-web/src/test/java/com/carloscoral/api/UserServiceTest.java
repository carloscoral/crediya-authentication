package com.carloscoral.api;

import com.carloscoral.api.dto.CreateUserRequest;
import com.carloscoral.api.exception.ValidationException;
import com.carloscoral.api.mapper.UserMapper;
import com.carloscoral.api.validation.GenericValidator;
import com.carloscoral.model.user.User;
import com.carloscoral.usecase.createuser.CreateUserUseCase;
import com.carloscoral.usecase.exception.DuplicateUserException;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private CreateUserUseCase createUserUseCase;

    @Mock
    private UserMapper userMapper;

    @Mock
    private GenericValidator validator;

    private CreateUserRequest validRequest;
    private User user;

    @BeforeEach
    void setUp() {
        validRequest = CreateUserRequest.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        user = User.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(validator.validate(any(CreateUserRequest.class))).thenReturn(Mono.just(validRequest));
        when(userMapper.toUser(any(CreateUserRequest.class))).thenReturn(user);
        when(createUserUseCase.execute(any(User.class))).thenReturn(Mono.empty());

        StepVerifier.create(userService.createUser(validRequest))
                .expectNext("User created successfully")
                .verifyComplete();

        verify(validator).validate(validRequest);
        verify(userMapper).toUser(validRequest);
        verify(createUserUseCase).execute(user);
    }

    @Test
    void shouldReturnErrorWhenRequestIsNull() {
        StepVerifier.create(userService.createUser(null))
                .expectErrorMatches(throwable -> {
                    if (throwable instanceof ValidationException) {
                        ValidationException ve = (ValidationException) throwable;
                        return "Request body is required".equals(ve.getMessage()) &&
                                ve.getValidationErrors().contains("request: Request body cannot be empty");
                    }
                    return false;
                })
                .verify();

        verify(validator, never()).validate(any());
        verify(userMapper, never()).toUser(any());
        verify(createUserUseCase, never()).execute(any());
    }

    @Test
    void shouldHandleValidationError() {
        List<String> errors = List.of(
                "firstName: First name is required",
                "email: Invalid email format"
        );
        ValidationException validationException = new ValidationException("Validation failed", errors);

        when(validator.validate(any(CreateUserRequest.class))).thenReturn(Mono.error(validationException));

        StepVerifier.create(userService.createUser(validRequest))
                .expectErrorMatches(throwable -> {
                    if (throwable instanceof ValidationException) {
                        ValidationException ve = (ValidationException) throwable;
                        return "Validation failed".equals(ve.getMessage()) &&
                                ve.getValidationErrors().equals(errors);
                    }
                    return false;
                })
                .verify();

        verify(validator).validate(validRequest);
        verify(userMapper, never()).toUser(any());
        verify(createUserUseCase, never()).execute(any());
    }

    @Test
    void shouldHandleMapperError() {
        RuntimeException mapperException = new RuntimeException("Mapping error");

        when(validator.validate(any(CreateUserRequest.class))).thenReturn(Mono.just(validRequest));
        when(userMapper.toUser(any(CreateUserRequest.class))).thenThrow(mapperException);

        StepVerifier.create(userService.createUser(validRequest))
                .expectError(RuntimeException.class)
                .verify();

        verify(validator).validate(validRequest);
        verify(userMapper).toUser(validRequest);
        verify(createUserUseCase, never()).execute(any());
    }

    @Test
    void shouldHandleDuplicateUserException() {
        DuplicateUserException duplicateException = new DuplicateUserException();

        when(validator.validate(any(CreateUserRequest.class))).thenReturn(Mono.just(validRequest));
        when(userMapper.toUser(any(CreateUserRequest.class))).thenReturn(user);
        when(createUserUseCase.execute(any(User.class))).thenReturn(Mono.error(duplicateException));

        StepVerifier.create(userService.createUser(validRequest))
                .expectErrorMatches(throwable -> {
                    if (throwable instanceof DuplicateUserException) {
                        return "User already exists".equals(throwable.getMessage());
                    }
                    return false;
                })
                .verify();

        verify(validator).validate(validRequest);
        verify(userMapper).toUser(validRequest);
        verify(createUserUseCase).execute(user);
    }

    @Test
    void shouldHandleUseCaseError() {
        RuntimeException useCaseException = new RuntimeException("Database connection error");

        when(validator.validate(any(CreateUserRequest.class))).thenReturn(Mono.just(validRequest));
        when(userMapper.toUser(any(CreateUserRequest.class))).thenReturn(user);
        when(createUserUseCase.execute(any(User.class))).thenReturn(Mono.error(useCaseException));

        StepVerifier.create(userService.createUser(validRequest))
                .expectError(RuntimeException.class)
                .verify();

        verify(validator).validate(validRequest);
        verify(userMapper).toUser(validRequest);
        verify(createUserUseCase).execute(user);
    }

    @Test
    void shouldCreateInstanceWithCorrectDependencies() {
        UserService service = new UserService(createUserUseCase, userMapper, validator);

        assert service != null;
    }

    @Test
    void shouldHandleEmptyValidationErrorsList() {
        ValidationException validationException = new ValidationException("Validation failed", List.of());

        when(validator.validate(any(CreateUserRequest.class))).thenReturn(Mono.error(validationException));

        StepVerifier.create(userService.createUser(validRequest))
                .expectErrorMatches(throwable -> {
                    if (throwable instanceof ValidationException) {
                        ValidationException ve = (ValidationException) throwable;
                        return "Validation failed".equals(ve.getMessage()) &&
                                ve.getValidationErrors().isEmpty();
                    }
                    return false;
                })
                .verify();

        verify(validator).validate(validRequest);
        verify(userMapper, never()).toUser(any());
        verify(createUserUseCase, never()).execute(any());
    }

    @Test
    void shouldCallAllDependenciesInCorrectOrder() {
        when(validator.validate(any(CreateUserRequest.class))).thenReturn(Mono.just(validRequest));
        when(userMapper.toUser(any(CreateUserRequest.class))).thenReturn(user);
        when(createUserUseCase.execute(any(User.class))).thenReturn(Mono.empty());

        StepVerifier.create(userService.createUser(validRequest))
                .expectNext("User created successfully")
                .verifyComplete();

        // Verify order of calls using InOrder
        var inOrder = inOrder(validator, userMapper, createUserUseCase);
        inOrder.verify(validator).validate(validRequest);
        inOrder.verify(userMapper).toUser(validRequest);
        inOrder.verify(createUserUseCase).execute(user);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldHandleRequestWithMinimalValidData() {
        CreateUserRequest minimalRequest = CreateUserRequest.builder()
                .firstName("Test")
                .lastName("User")
                .birthday(LocalDate.of(2000, 1, 1))
                .address("Test Address")
                .email("test@example.com")
                .identification("123")
                .phone("+1234567890")
                .baseSalary(new BigDecimal("1000"))
                .build();

        User minimalUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .birthday(LocalDate.of(2000, 1, 1))
                .address("Test Address")
                .email("test@example.com")
                .identification("123")
                .phone("+1234567890")
                .baseSalary(new BigDecimal("1000"))
                .build();

        when(validator.validate(any(CreateUserRequest.class))).thenReturn(Mono.just(minimalRequest));
        when(userMapper.toUser(any(CreateUserRequest.class))).thenReturn(minimalUser);
        when(createUserUseCase.execute(any(User.class))).thenReturn(Mono.empty());

        StepVerifier.create(userService.createUser(minimalRequest))
                .expectNext("User created successfully")
                .verifyComplete();

        verify(validator).validate(minimalRequest);
        verify(userMapper).toUser(minimalRequest);
        verify(createUserUseCase).execute(minimalUser);
    }
}
