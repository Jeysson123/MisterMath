package com.math.api.dto;

import com.math.api.validation.SupportedOperation;
import com.math.api.validation.SupportedPattern;
import com.math.application.command.CalculateCommand;
import com.math.domain.pattern.CalculationPattern;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Payload de {@code POST /api/v1/calculations}.
 *
 * <pre>
 *  {
 *    "num1": 10,
 *    "num2": 5,
 *    "operation": "+",          ← + - * / % ^
 *    "pattern": "STRATEGY"      ← SINGLETON | FACTORY | STRATEGY | BUILDER
 *  }
 * </pre>
 *
 * <p>Las anotaciones de {@code jakarta.validation} se evalúan gracias a {@code @Valid} en el
 * controlador; si alguna falla, el {@code GlobalExceptionHandler} responde 400 con el detalle
 * por campo. {@link SupportedOperation} y {@link SupportedPattern} son validaciones propias.</p>
 *
 * <p>Lombok: {@code @Data} genera getters, setters, {@code equals}, {@code hashCode} y
 * {@code toString}; Jackson usa el constructor vacío y los setters para leer el JSON.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CalculationRequest {

    @NotNull(message = "num1 is required")
    @Digits(integer = 30, fraction = 10, message = "num1 allows up to 30 integer digits and 10 decimals")
    private BigDecimal num1;

    @NotNull(message = "num2 is required")
    @Digits(integer = 30, fraction = 10, message = "num2 allows up to 30 integer digits and 10 decimals")
    private BigDecimal num2;

    @NotBlank(message = "operation is required")
    @SupportedOperation
    private String operation;

    @NotBlank(message = "pattern is required")
    @SupportedPattern
    private String pattern;

    /**
     * Convierte el payload HTTP en el comando de CQRS.
     *
     * @return el comando listo para el handler
     */
    public CalculateCommand toCommand() {
        return new CalculateCommand(num1, num2, operation, CalculationPattern.from(pattern));
    }
}
