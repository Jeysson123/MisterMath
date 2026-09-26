package com.math.api.validation;

import com.math.domain.operation.OperationType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Lógica de {@link SupportedOperation}.
 *
 * <p>Un valor {@code null} se considera válido: de eso se encarga {@code @NotBlank}. Así cada
 * anotación valida una sola cosa (Single Responsibility también aplica a las validaciones).</p>
 */
public class SupportedOperationValidator implements ConstraintValidator<SupportedOperation, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return value == null || value.isBlank() || OperationType.isSupported(value);
    }
}
