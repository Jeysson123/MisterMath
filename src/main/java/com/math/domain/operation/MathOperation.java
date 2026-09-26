package com.math.domain.operation;

import com.math.domain.exception.MathDomainException;

import java.math.BigDecimal;

/**
 * Contrato de una operación matemática entre dos números.
 *
 * <h2>SOLID aplicado aquí</h2>
 * <ul>
 *   <li><b>O - Open/Closed:</b> para soportar una operación nueva se crea otra clase que
 *       implemente esta interfaz; las existentes no se tocan.</li>
 *   <li><b>L - Liskov:</b> cualquier implementación puede usarse donde se espera un
 *       {@code MathOperation} sin romper al llamador. Por eso el contrato declara de antemano
 *       que {@link #apply} puede lanzar {@link MathDomainException}: ninguna subclase
 *       "sorprende" con un error distinto.</li>
 *   <li><b>Polimorfismo:</b> quien llama a {@code operation.apply(a, b)} no sabe qué clase real
 *       hay detrás; la JVM elige la implementación en tiempo de ejecución
 *       (<i>dynamic method dispatch</i>).</li>
 * </ul>
 *
 * <pre>
 *                 MathOperation
 *      ┌────────┬───────┼────────┬────────┬───────┐
 *  Addition  Subtraction  Multiplication  Division  Modulo  Power
 * </pre>
 */
public interface MathOperation {

    /**
     * Tipo (y símbolo) que esta implementación resuelve.
     *
     * @return el {@link OperationType} asociado
     */
    OperationType type();

    /**
     * Ejecuta la operación.
     *
     * @param left  primer operando ({@code num1})
     * @param right segundo operando ({@code num2})
     * @return el resultado, sin ceros sobrantes a la derecha
     * @throws MathDomainException si los operandos no son válidos para esta operación
     *                             (por ejemplo, dividir entre cero)
     */
    BigDecimal apply(BigDecimal left, BigDecimal right);
}
