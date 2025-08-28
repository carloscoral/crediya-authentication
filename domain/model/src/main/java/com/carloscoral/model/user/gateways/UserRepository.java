package com.carloscoral.model.user.gateways;

import com.carloscoral.model.user.User;
import reactor.core.publisher.Mono;

public interface UserRepository {
    Mono<User> saveUser(User user);
    Mono<User> findExistentUser(String email, String identification);
    Mono<User> findByEmail(String email);
}
