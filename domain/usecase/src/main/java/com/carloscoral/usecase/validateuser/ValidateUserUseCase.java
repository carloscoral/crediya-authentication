package com.carloscoral.usecase.validateuser;

import com.carloscoral.model.user.gateways.UserRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ValidateUserUseCase {
    private final UserRepository userRepository;

    public Mono<Boolean> byEmail(String email) {
        return userRepository.findByEmail(email)
                .hasElement();
    }
}
