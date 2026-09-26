package com.math.domain.pattern.factory;

import com.math.domain.operation.Addition;
import com.math.domain.operation.Division;
import com.math.domain.operation.MathOperation;
import com.math.domain.operation.Modulo;
import com.math.domain.operation.Multiplication;
import com.math.domain.operation.OperationType;
import com.math.domain.operation.Power;
import com.math.domain.operation.Subtraction;

/**
 * <b>Patrón Factory (Simple Factory / Factory Method estático).</b>
 *
 * <p>Centraliza el {@code new}: el resto del código pide "dame la operación de {@code +}" y
 * recibe un {@link MathOperation}, sin conocer la clase concreta. Si mañana {@code Addition}
 * cambia de constructor, solo se toca este archivo.</p>
 *
 * <pre>
 *   create("+") ──► new Addition()
 *   create("/") ──► new Division()
 *   create("^") ──► new Power()
 * </pre>
 *
 * <p>Cada llamada devuelve una instancia <b>nueva</b> (a diferencia del Singleton).</p>
 */
public final class MathOperationFactory {

    /** Clase utilitaria: no se instancia. */
    private MathOperationFactory() {
    }

    /**
     * Crea la operación correspondiente a un símbolo.
     *
     * @param symbol símbolo recibido en el payload
     * @return una instancia nueva de la operación
     * @throws com.math.domain.exception.UnsupportedOperationSymbolException si el símbolo no existe
     */
    public static MathOperation create(String symbol) {
        return create(OperationType.fromSymbol(symbol));
    }

    /**
     * Crea la operación correspondiente a un tipo.
     *
     * <p>El {@code switch} sobre un enum es exhaustivo: si se agrega un {@link OperationType} y se
     * olvida su caso, el código deja de compilar. Es el único lugar que "conoce" las clases
     * concretas.</p>
     *
     * @param type tipo de operación
     * @return una instancia nueva de la operación
     */
    public static MathOperation create(OperationType type) {
        return switch (type) {
            case ADDITION -> new Addition();
            case SUBTRACTION -> new Subtraction();
            case MULTIPLICATION -> new Multiplication();
            case DIVISION -> new Division();
            case MODULO -> new Modulo();
            case POWER -> new Power();
        };
    }
}
