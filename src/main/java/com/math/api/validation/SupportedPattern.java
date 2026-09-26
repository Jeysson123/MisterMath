package com.math.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validación propia: el campo debe ser un valor de
 * {@link com.math.domain.pattern.CalculationPattern} (sin importar mayúsculas).
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SupportedPatternValidator.class)
public @interface SupportedPattern {

    /** @return mensaje si falla */
    String message() default "pattern must be one of SINGLETON, FACTORY, STRATEGY, BUILDER";

    /** @return grupos de validación (estándar de Jakarta Validation) */
    Class<?>[] groups() default {};

    /** @return metadatos adicionales (estándar de Jakarta Validation) */
    Class<? extends Payload>[] payload() default {};
}
