package com.carloscoral.api.validation;

import com.carloscoral.api.exception.ValidationException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class GenericValidator {
    
    private final Validator validator;

    public <T> Mono<T> validate(T object) {
        return Mono.fromCallable(() -> {
            log.debug("Validating object of type: {}", object.getClass().getSimpleName());
            
            Set<ConstraintViolation<T>> violations = validator.validate(object);
            
            if (!violations.isEmpty()) {
                List<String> validationErrors = violations.stream()
                    .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
                    .toList();
                
                log.warn("Validation errors found: {}", validationErrors);
                throw new ValidationException(validationErrors);
            }
            
            log.debug("Validation successful for object of type: {}", object.getClass().getSimpleName());
            return object;
        });
    }
}
