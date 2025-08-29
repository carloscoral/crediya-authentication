package com.carloscoral.r2dbc;

import com.carloscoral.model.user.User;
import com.carloscoral.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserReactiveRepositoryAdapterTest {

    @InjectMocks
    UserReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    UserReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    private User sampleUser;
    private UserEntity sampleUserEntity;
    private UserEntity.UserEntityBuilder mockEntityBuilder;
    private User.UserBuilder mockUserBuilder;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        sampleUserEntity = UserEntity.builder()
                .id("user-id-123")
                .firstName("Carlos")
                .lastName("Coral")
                .birthday(LocalDate.of(1990, 5, 15))
                .address("Calle 123 #45-67, Bogotá")
                .email("carlos.coral@example.com")
                .identification("123456789")
                .phone("+573001234567")
                .baseSalary(new BigDecimal("5000000"))
                .build();

        mockEntityBuilder = mock(UserEntity.UserEntityBuilder.class);
        mockUserBuilder = mock(User.UserBuilder.class);
    }

    @Test
    void shouldSaveUserSuccessfully() {
        when(mapper.mapBuilder(eq(sampleUser), eq(UserEntity.UserEntityBuilder.class)))
                .thenReturn(mockEntityBuilder);
        when(mockEntityBuilder.build()).thenReturn(sampleUserEntity);
        when(repository.save(sampleUserEntity)).thenReturn(Mono.just(sampleUserEntity));
        when(mapper.mapBuilder(eq(sampleUserEntity), eq(User.UserBuilder.class)))
                .thenReturn(mockUserBuilder);
        when(mockUserBuilder.build()).thenReturn(sampleUser);

        Mono<User> result = repositoryAdapter.saveUser(sampleUser);

        StepVerifier.create(result)
                .expectNext(sampleUser)
                .verifyComplete();

        verify(mapper).mapBuilder(sampleUser, UserEntity.UserEntityBuilder.class);
        verify(repository).save(sampleUserEntity);
        verify(mapper).mapBuilder(sampleUserEntity, User.UserBuilder.class);
    }

    @Test
    void shouldHandleErrorWhenSavingUser() {
        RuntimeException exception = new RuntimeException("Database error");
        when(mapper.mapBuilder(eq(sampleUser), eq(UserEntity.UserEntityBuilder.class)))
                .thenReturn(mockEntityBuilder);
        when(mockEntityBuilder.build()).thenReturn(sampleUserEntity);
        when(repository.save(sampleUserEntity)).thenReturn(Mono.error(exception));

        Mono<User> result = repositoryAdapter.saveUser(sampleUser);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(mapper).mapBuilder(sampleUser, UserEntity.UserEntityBuilder.class);
        verify(repository).save(sampleUserEntity);
        verify(mapper, never()).mapBuilder(any(UserEntity.class), eq(User.UserBuilder.class));
    }

    @Test
    void shouldFindExistentUserByEmailOrIdentification() {
        String email = "carlos.coral@example.com";
        String identification = "123456789";
        
        when(repository.findByEmailOrIdentification(email, identification))
                .thenReturn(Mono.just(sampleUserEntity));
        when(mapper.mapBuilder(eq(sampleUserEntity), eq(User.UserBuilder.class)))
                .thenReturn(mockUserBuilder);
        when(mockUserBuilder.build()).thenReturn(sampleUser);

        Mono<User> result = repositoryAdapter.findExistentUser(email, identification);

        StepVerifier.create(result)
                .expectNext(sampleUser)
                .verifyComplete();

        verify(repository).findByEmailOrIdentification(email, identification);
        verify(mapper).mapBuilder(sampleUserEntity, User.UserBuilder.class);
    }

    @Test
    void shouldReturnEmptyWhenUserNotFound() {
        String email = "nonexistent@example.com";
        String identification = "999999999";
        
        when(repository.findByEmailOrIdentification(email, identification))
                .thenReturn(Mono.empty());

        Mono<User> result = repositoryAdapter.findExistentUser(email, identification);

        StepVerifier.create(result)
                .verifyComplete();

        verify(repository).findByEmailOrIdentification(email, identification);
        verify(mapper, never()).mapBuilder(any(UserEntity.class), eq(User.UserBuilder.class));
    }

    @Test
    void shouldHandleErrorWhenFindingUser() {
        String email = "error@example.com";
        String identification = "123456789";
        RuntimeException exception = new RuntimeException("Database connection error");
        
        when(repository.findByEmailOrIdentification(email, identification))
                .thenReturn(Mono.error(exception));

        Mono<User> result = repositoryAdapter.findExistentUser(email, identification);

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(repository).findByEmailOrIdentification(email, identification);
        verify(mapper, never()).mapBuilder(any(UserEntity.class), eq(User.UserBuilder.class));
    }

    @Test
    void shouldCreateInstanceWithCorrectDependencies() {
        UserReactiveRepositoryAdapter adapter = new UserReactiveRepositoryAdapter(repository, mapper);

        assert adapter != null;
    }

    @Test
    void shouldInitializeSuperClassWithCorrectMapper() {
        UserEntity entityFromRepo = UserEntity.builder()
                .id("test-id")
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .build();

        User expectedUser = User.builder()
                .firstName("Test")
                .lastName("User")
                .email("test@example.com")
                .build();

        when(mapper.map(entityFromRepo, User.class)).thenReturn(expectedUser);
        when(repository.findById("test-id")).thenReturn(Mono.just(entityFromRepo));

        UserReactiveRepositoryAdapter adapter = new UserReactiveRepositoryAdapter(repository, mapper);
        
        Mono<User> result = adapter.findById("test-id");

        StepVerifier.create(result)
                .expectNext(expectedUser)
                .verifyComplete();

        verify(mapper).map(entityFromRepo, User.class);
        verify(repository).findById("test-id");
    }

    @Test
    void shouldHandleMappingErrorInSuperClassLambda() {
        UserEntity entityFromRepo = UserEntity.builder()
                .id("test-id")
                .firstName("Test")
                .build();

        RuntimeException mappingException = new RuntimeException("Mapping error");
        when(mapper.map(entityFromRepo, User.class)).thenThrow(mappingException);
        when(repository.findById("test-id")).thenReturn(Mono.just(entityFromRepo));

        UserReactiveRepositoryAdapter adapter = new UserReactiveRepositoryAdapter(repository, mapper);
        Mono<User> result = adapter.findById("test-id");

        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(mapper).map(entityFromRepo, User.class);
    }

    @Test
    void shouldHandleNullUserInSaveUser() {
        when(mapper.mapBuilder(eq(null), eq(UserEntity.UserEntityBuilder.class)))
                .thenReturn(mockEntityBuilder);
        when(mockEntityBuilder.build()).thenReturn(null);
        when(repository.save(null)).thenReturn(Mono.error(new IllegalArgumentException("Entity cannot be null")));

        Mono<User> result = repositoryAdapter.saveUser(null);

        StepVerifier.create(result)
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void shouldHandleNullParametersInFindExistentUser() {
        when(repository.findByEmailOrIdentification(null, null))
                .thenReturn(Mono.empty());

        Mono<User> result = repositoryAdapter.findExistentUser(null, null);

        StepVerifier.create(result)
                .verifyComplete();

        verify(repository).findByEmailOrIdentification(null, null);
    }
}