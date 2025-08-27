package co.com.pragma.api.validador;

import co.com.pragma.api.errores.ErrorValidacion;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.Validator;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class ValidadorRequest {

    private final Validator validator;

    public ValidadorRequest(Validator validator) {
        this.validator = validator;
    }

    public <T> Mono<T> validar(T dto) {
        return Mono.fromCallable(() -> {
            var errors = new BeanPropertyBindingResult(dto, dto.getClass().getName());
            validator.validate(dto, errors);
            if (errors.hasErrors()){
                Set<String> mensajesDeError = errors.getFieldErrors().stream()
                        .map(FieldError::getDefaultMessage)
                        .collect(Collectors.toSet());
                throw new ErrorValidacion("Validacion fallida", mensajesDeError);
            }
            return dto;
        });
    }
}
