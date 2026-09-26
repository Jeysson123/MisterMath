package com.math.api.validation;

import com.math.domain.pattern.CalculationPattern;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.Arrays;

/**
 * Lógica de {@link SupportedPattern}.
 */
public class SupportedPatternValidator implements ConstraintValidator<SupportedPattern, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return Arrays.stream(CalculationPattern.values())
                .anyMatch(pattern -> pattern.name().equalsIgnoreCase(value.trim()));
    }
}
