package com.carloscoral.r2dbc;

import com.carloscoral.model.user.User;
import com.carloscoral.r2dbc.entity.UserEntity;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.reactivecommons.utils.ObjectMapper;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserReactiveRepositoryAdapterTest {
    @InjectMocks
    UserReactiveRepositoryAdapter repositoryAdapter;

    @Mock
    UserReactiveRepository repository;

    @Mock
    ObjectMapper mapper;

    void mustSaveValue() {
        User user = User.builder().build();
        UserEntity userEntity = new UserEntity();
        when(repository.save(userEntity)).thenReturn(Mono.just(userEntity));
        when(mapper.map(user, User.class)).thenReturn(user);

        Mono<User> result = repositoryAdapter.saveUser(user);

        // TODO Fix tests
        StepVerifier.create(result)
                .expectNextMatches(value -> value.equals("test"))
                .verifyComplete();
    }
}
