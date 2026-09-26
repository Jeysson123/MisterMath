package com.math.api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Validación propia: el campo debe ser un símbolo de
 * {@link com.math.domain.operation.OperationType}.
 *
 * <p>Una anotación de validación tiene dos piezas: esta anotación (el "qué") y
 * {@link SupportedOperationValidator} (el "cómo"), unidas por {@code @Constraint}.</p>
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SupportedOperationValidator.class)
public @interface SupportedOperation {

    /** @return mensaje si falla */
    String message() default "operation must be one of + - * / % ^";

    /** @return grupos de validación (estándar de Jakarta Validation) */
    Class<?>[] groups() default {};

    /** @return metadatos adicionales (estándar de Jakarta Validation) */
    Class<? extends Payload>[] payload() default {};
}
