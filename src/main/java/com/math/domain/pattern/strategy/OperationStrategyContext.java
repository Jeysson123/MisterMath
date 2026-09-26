package com.math.domain.pattern.strategy;

import com.math.domain.exception.UnsupportedOperationSymbolException;
import com.math.domain.operation.MathOperation;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <b>Patrón Strategy</b> — el "contexto".
 *
 * <p>Una estrategia es un algoritmo intercambiable. Aquí cada {@link MathOperation} anotada con
 * {@code @Component} es una estrategia; Spring las inyecta todas en una lista y este contexto
 * las indexa por símbolo. En tiempo de ejecución se escoge una según el payload.</p>
 *
 * <pre>
 *  List&lt;MathOperation&gt; (Spring)
 *        │
 *        ▼
 *  { "+": Addition, "-": Subtraction, "*": Multiplication, "/": Division, ... }
 *        │  strategyFor("*")
 *        ▼
 *   Multiplication
 * </pre>
 *
 * <p>Diferencia con Factory: aquí nadie hace {@code new}; las estrategias ya existen como beans y
 * solo se <i>seleccionan</i>. Una operación nueva con {@code @Component} aparece sola en el mapa.</p>
 */
@Component
public class OperationStrategyContext {

    private final Map<String, MathOperation> strategies = new HashMap<>();

    /**
     * @param operations todas las operaciones registradas como beans
     */
    public OperationStrategyContext(List<MathOperation> operations) {
        operations.forEach(operation -> strategies.put(operation.type().getSymbol(), operation));
    }

    /**
     * Selecciona la estrategia de un símbolo.
     *
     * @param symbol símbolo de la operación
     * @return la estrategia registrada
     * @throws UnsupportedOperationSymbolException si ningún bean atiende ese símbolo
     */
    public MathOperation strategyFor(String symbol) {
        MathOperation strategy = strategies.get(symbol);
        if (strategy == null) {
            throw new UnsupportedOperationSymbolException(symbol);
        }
        return strategy;
    }
}
