package com.math.application.command;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.math.domain.pattern.CalculationPattern;
import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

/**
 * Resultado del comando de cálculo; es el {@code data} de la respuesta del POST.
 *
 * <p>{@code @Builder} de Lombok genera {@code CalculationResult.builder()...build()} y
 * {@code toBuilder = true} permite copiar el objeto cambiando un campo (se usa para agregar el
 * {@code transactionId} después de guardar).</p>
 *
 * @param transactionId id en PostgreSQL, úsalo en {@code GET /api/v1/transactions/{id}}. Se omite
 *                      del JSON cuando es nulo (el response guardado se serializa antes de tener id)
 * @param num1          primer número
 * @param num2          segundo número
 * @param operation     símbolo usado
 * @param pattern       patrón usado
 * @param result        resultado de la operación
 * @param engine        clase que resolvió el cálculo
 * @param trace         pasos que siguió el patrón
 */
@Builder(toBuilder = true)
public record CalculationResult(
        @JsonInclude(JsonInclude.Include.NON_NULL) Long transactionId,
        BigDecimal num1,
        BigDecimal num2,
        String operation,
        CalculationPattern pattern,
        BigDecimal result,
        String engine,
        List<String> trace) {
}
