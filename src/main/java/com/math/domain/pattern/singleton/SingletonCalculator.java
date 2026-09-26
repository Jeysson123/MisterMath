package com.math.domain.pattern.singleton;

import com.math.domain.operation.MathOperation;
import com.math.domain.operation.OperationType;
import com.math.domain.pattern.factory.MathOperationFactory;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

/**
 * <b>Patrón Singleton</b> (variante "Initialization-on-demand holder", de Bill Pugh).
 *
 * <p>Garantiza que en toda la JVM exista <b>una sola</b> calculadora. Sus tres ingredientes:</p>
 * <ol>
 *   <li>Constructor {@code private}: nadie puede hacer {@code new SingletonCalculator()}.</li>
 *   <li>Una clase interna estática {@link Holder} que guarda la instancia. La JVM solo la carga
 *       la primera vez que se llama a {@link #getInstance()} (carga perezosa).</li>
 *   <li>La carga de clases en Java es thread-safe, así que no hace falta {@code synchronized}.</li>
 * </ol>
 *
 * <p>Al crearse, arma una sola vez su tabla de operaciones y la reutiliza en cada cálculo.</p>
 *
 * <p><i>Nota:</i> los beans de Spring ya son singletons por defecto; esta clase muestra cómo se
 * implementa el patrón "a mano", sin framework.</p>
 */
public final class SingletonCalculator {

    private final Map<OperationType, MathOperation> operations;

    /** Privado: la única forma de obtener la calculadora es {@link #getInstance()}. */
    private SingletonCalculator() {
        Map<OperationType, MathOperation> table = new EnumMap<>(OperationType.class);
        for (OperationType type : OperationType.values()) {
            table.put(type, MathOperationFactory.create(type));
        }
        this.operations = Collections.unmodifiableMap(table);
    }

    /** La JVM inicializa esta clase (y por tanto la instancia) solo cuando se usa. */
    private static final class Holder {
        private static final SingletonCalculator INSTANCE = new SingletonCalculator();
    }

    /**
     * Punto de acceso global a la única instancia.
     *
     * @return siempre el mismo objeto
     */
    public static SingletonCalculator getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Resuelve la operación reutilizando los objetos creados al inicializar el singleton.
     *
     * @param num1   primer número
     * @param symbol símbolo de la operación
     * @param num2   segundo número
     * @return el resultado
     */
    public BigDecimal calculate(BigDecimal num1, String symbol, BigDecimal num2) {
        return operations.get(OperationType.fromSymbol(symbol)).apply(num1, num2);
    }
}
