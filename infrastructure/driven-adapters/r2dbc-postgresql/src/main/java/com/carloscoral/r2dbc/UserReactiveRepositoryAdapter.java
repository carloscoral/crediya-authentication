package com.carloscoral.r2dbc;

import com.carloscoral.model.user.User;
import com.carloscoral.model.user.gateways.UserRepository;
import com.carloscoral.r2dbc.entity.UserEntity;
import com.carloscoral.r2dbc.helper.ReactiveAdapterOperations;

import lombok.extern.slf4j.Slf4j;

import org.reactivecommons.utils.ObjectMapper;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
@Slf4j
public class UserReactiveRepositoryAdapter extends ReactiveAdapterOperations<
        User,
        UserEntity,
        String,
        UserReactiveRepository
        > implements UserRepository {
    public UserReactiveRepositoryAdapter(UserReactiveRepository repository, ObjectMapper mapper) {
        /**
         *  Could be use mapper.mapBuilder if your domain model implement builder pattern
         *  super(repository, mapper, d -> mapper.mapBuilder(d,ObjectModel.ObjectModelBuilder.class).build());
         *  Or using mapper.map with the class of the object model
         */
        super(repository, mapper, d -> mapper.map(d, User.class));
    }

    @Override
    public Mono<User> saveUser(User user) {
        UserEntity userEntity = mapper.mapBuilder(user, UserEntity.UserEntityBuilder.class).build();
        return repository.save(userEntity).map(entity -> mapper.mapBuilder(entity, User.UserBuilder.class).build());
    }

    @Override
    public Mono<User> findExistentUser(String email, String identification) {
        return repository.findByEmailOrIdentification(email, identification)
            .map(entity -> mapper.mapBuilder(entity, User.UserBuilder.class).build());
    }
}
