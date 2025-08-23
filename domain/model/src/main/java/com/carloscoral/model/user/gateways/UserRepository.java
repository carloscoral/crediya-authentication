package com.carloscoral.model.user.gateways;

import com.carloscoral.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<Void> saveUser(User user);
}
