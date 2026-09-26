package com.math.domain.operation;

import com.math.domain.exception.UnsupportedOperationSymbolException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;

/**
 * Catálogo de operaciones que MisterMath sabe resolver, identificadas por su símbolo.
 *
 * <p>Es la única "lista oficial" de símbolos: la validación del request, la fábrica y el
 * singleton la consultan, así que agregar una operación empieza aquí.</p>
 *
 * <pre>
 *  "+"  → ADDITION        "/"  → DIVISION
 *  "-"  → SUBTRACTION     "%"  → MODULO
 *  "*"  → MULTIPLICATION  "^"  → POWER
 * </pre>
 */
@Getter
@RequiredArgsConstructor
public enum OperationType {

    ADDITION("+"),
    SUBTRACTION("-"),
    MULTIPLICATION("*"),
    DIVISION("/"),
    MODULO("%"),
    POWER("^");

    /** Símbolo matemático que el cliente envía en el campo {@code operation}. */
    private final String symbol;

    /**
     * Traduce un símbolo a su tipo de operación.
     *
     * @param symbol símbolo recibido, por ejemplo {@code "+"}
     * @return el {@link OperationType} correspondiente
     * @throws UnsupportedOperationSymbolException si el símbolo no existe en el catálogo
     */
    public static OperationType fromSymbol(String symbol) {
        return Arrays.stream(values())
                .filter(type -> type.symbol.equals(symbol))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationSymbolException(symbol));
    }

    /**
     * Indica si un símbolo está soportado, sin lanzar excepciones (útil para validaciones).
     *
     * @param symbol símbolo a comprobar
     * @return {@code true} si existe en el catálogo
     */
    public static boolean isSupported(String symbol) {
        return Arrays.stream(values()).anyMatch(type -> type.symbol.equals(symbol));
    }

    /**
     * Lista de símbolos soportados, usada en los mensajes de error.
     *
     * @return símbolos en el orden del enum
     */
    public static List<String> symbols() {
        return Arrays.stream(values()).map(OperationType::getSymbol).toList();
    }
}
