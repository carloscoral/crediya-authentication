package com.carloscoral.usecase.createuser;

import com.carloscoral.model.user.User;
import com.carloscoral.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class CreateUserUseCase {
    private final UserRepository userRepository;

    public Mono<Void> execute(User user) {
        return userRepository.saveUser(user);
    }
}
