package com.carloscoral.api.config;

import com.carloscoral.api.UserController;
import com.carloscoral.api.UserService;
import com.carloscoral.api.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;

@ContextConfiguration(classes = {UserController.class, GlobalExceptionHandler.class})
@WebFluxTest
@Import({CorsConfig.class, SecurityHeadersConfig.class, ConfigTest.TestConfig.class})
class ConfigTest {

    @Autowired
    private WebTestClient webTestClient;

    @TestConfiguration
    static class TestConfig {
        @Bean
        UserService userService() {
            return org.mockito.Mockito.mock(UserService.class);
        }
    }

    @Test
    void corsConfigurationShouldAllowOrigins() {
        webTestClient.options()
                .uri("/api/v1/users")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy",
                        "default-src 'self'; frame-ancestors 'self'; form-action 'self'")
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000;")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().valueEquals("Server", "")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Pragma", "no-cache")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

}