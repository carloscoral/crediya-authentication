package com.carloscoral.usecase.createuser;

import com.carloscoral.model.user.User;
import com.carloscoral.model.user.gateways.UserRepository;
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    @Mock
    private UserRepository userRepository;

    private User user;
    private User existingUser;

    @BeforeEach
    void setUp() {
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

        existingUser = User.builder()
                .firstName("Existing")
                .lastName("User")
                .birthday(LocalDate.of(1985, 1, 1))
                .address("Existing Address")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573009999999")
                .baseSalary(new BigDecimal("3000000"))
                .build();
    }

    @Test
    void shouldCreateUserSuccessfully() {
        when(userRepository.findExistentUser(user.getEmail(), user.getIdentification()))
                .thenReturn(Mono.empty());
        when(userRepository.saveUser(user)).thenReturn(Mono.just(user));

        StepVerifier.create(createUserUseCase.execute(user))
                .verifyComplete();

        verify(userRepository).findExistentUser(user.getEmail(), user.getIdentification());
        verify(userRepository).saveUser(user);
    }

    @Test
    void shouldThrowDuplicateUserExceptionWhenUserExists() {
        when(userRepository.findExistentUser(user.getEmail(), user.getIdentification()))
                .thenReturn(Mono.just(existingUser));
        when(userRepository.saveUser(user)).thenReturn(Mono.just(user));

        StepVerifier.create(createUserUseCase.execute(user))
                .expectErrorMatches(throwable -> {
                    if (throwable instanceof DuplicateUserException) {
                        return "User already exists".equals(throwable.getMessage());
                    }
                    return false;
                })
                .verify();

        verify(userRepository).findExistentUser(user.getEmail(), user.getIdentification());
    }

    @Test
    void shouldHandleErrorWhenFindingExistentUser() {
        RuntimeException repositoryException = new RuntimeException("Database connection error");
        when(userRepository.findExistentUser(user.getEmail(), user.getIdentification()))
                .thenReturn(Mono.error(repositoryException));
        when(userRepository.saveUser(user)).thenReturn(Mono.just(user));

        StepVerifier.create(createUserUseCase.execute(user))
                .expectError(RuntimeException.class)
                .verify();

        verify(userRepository).findExistentUser(user.getEmail(), user.getIdentification());
    }

    @Test
    void shouldHandleErrorWhenSavingUser() {
        RuntimeException saveException = new RuntimeException("Failed to save user");
        when(userRepository.findExistentUser(user.getEmail(), user.getIdentification()))
                .thenReturn(Mono.empty());
        when(userRepository.saveUser(user)).thenReturn(Mono.error(saveException));

        StepVerifier.create(createUserUseCase.execute(user))
                .expectError(RuntimeException.class)
                .verify();

        verify(userRepository).findExistentUser(user.getEmail(), user.getIdentification());
        verify(userRepository).saveUser(user);
    }

    @Test
    void shouldCreateInstanceWithCorrectDependencies() {
        CreateUserUseCase useCase = new CreateUserUseCase(userRepository);

        assertNotNull(useCase);
    }

    @Test
    void shouldHandleUserWithSameEmail() {
        User userWithSameEmail = User.builder()
                .firstName("Different")
                .lastName("Person")
                .email("carlos.coral@example.com")
                .identification("987654321")
                .birthday(LocalDate.of(1995, 12, 25))
                .address("Different Address")
                .phone("+573007777777")
                .baseSalary(new BigDecimal("4000000"))
                .build();

        when(userRepository.findExistentUser(userWithSameEmail.getEmail(), userWithSameEmail.getIdentification()))
                .thenReturn(Mono.just(existingUser));
        when(userRepository.saveUser(userWithSameEmail)).thenReturn(Mono.just(userWithSameEmail));

        StepVerifier.create(createUserUseCase.execute(userWithSameEmail))
                .expectError(DuplicateUserException.class)
                .verify();

        verify(userRepository).findExistentUser(userWithSameEmail.getEmail(), userWithSameEmail.getIdentification());
    }

    @Test
    void shouldHandleUserWithSameIdentification() {
        User userWithSameId = User.builder()
                .firstName("Different")
                .lastName("Person")
                .email("different@example.com")
                .identification("123456789")
                .birthday(LocalDate.of(1995, 12, 25))
                .address("Different Address")
                .phone("+573007777777")
                .baseSalary(new BigDecimal("4000000"))
                .build();

        when(userRepository.findExistentUser(userWithSameId.getEmail(), userWithSameId.getIdentification()))
                .thenReturn(Mono.just(existingUser));
        when(userRepository.saveUser(userWithSameId)).thenReturn(Mono.just(userWithSameId));

        StepVerifier.create(createUserUseCase.execute(userWithSameId))
                .expectError(DuplicateUserException.class)
                .verify();

        verify(userRepository).findExistentUser(userWithSameId.getEmail(), userWithSameId.getIdentification());
    }

    @Test
    void shouldHandleNullUserFields() {
        User userWithNullFields = User.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .email(null)
                .identification(null)
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Test Address")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        when(userRepository.findExistentUser(null, null))
                .thenReturn(Mono.empty());
        when(userRepository.saveUser(userWithNullFields))
                .thenReturn(Mono.just(userWithNullFields));

        StepVerifier.create(createUserUseCase.execute(userWithNullFields))
                .verifyComplete();

        verify(userRepository).findExistentUser(null, null);
        verify(userRepository).saveUser(userWithNullFields);
    }

    @Test
    void shouldCallRepositoryMethodsInCorrectOrder() {
        when(userRepository.findExistentUser(user.getEmail(), user.getIdentification()))
                .thenReturn(Mono.empty());
        when(userRepository.saveUser(user)).thenReturn(Mono.just(user));

        StepVerifier.create(createUserUseCase.execute(user))
                .verifyComplete();

        var inOrder = inOrder(userRepository);
        inOrder.verify(userRepository).findExistentUser(user.getEmail(), user.getIdentification());
        inOrder.verify(userRepository).saveUser(user);
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldPropagateRepositoryExceptionsCorrectly() {
        IllegalArgumentException illegalArgException = new IllegalArgumentException("Invalid user data");
        when(userRepository.findExistentUser(user.getEmail(), user.getIdentification()))
                .thenReturn(Mono.empty());
        when(userRepository.saveUser(user)).thenReturn(Mono.error(illegalArgException));

        StepVerifier.create(createUserUseCase.execute(user))
                .expectErrorMatches(throwable -> 
                    throwable instanceof IllegalArgumentException &&
                    "Invalid user data".equals(throwable.getMessage()))
                .verify();

        verify(userRepository).findExistentUser(user.getEmail(), user.getIdentification());
        verify(userRepository).saveUser(user);
    }

    @Test
    void shouldHandleEmptyMonoFromFindExistentUser() {
        when(userRepository.findExistentUser(user.getEmail(), user.getIdentification()))
                .thenReturn(Mono.empty());
        when(userRepository.saveUser(user)).thenReturn(Mono.just(user));

        StepVerifier.create(createUserUseCase.execute(user))
                .verifyComplete();

        verify(userRepository).findExistentUser(user.getEmail(), user.getIdentification());
        verify(userRepository).saveUser(user);
    }
}
