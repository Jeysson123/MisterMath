package com.math.domain.pattern;

import com.math.domain.exception.UnsupportedPatternException;

import java.util.Arrays;

/**
 * Patrones que el cliente puede elegir en el campo {@code pattern} del payload.
 *
 * <p>Cada valor tiene un {@link CalculationEngine} que resuelve la operación a su manera:</p>
 * <pre>
 *  SINGLETON → una única instancia compartida de calculadora
 *  FACTORY   → una fábrica crea un objeto operación nuevo en cada llamada
 *  STRATEGY  → Spring inyecta todas las operaciones y se elige una en tiempo de ejecución
 *  BUILDER   → se arma una expresión inmutable paso a paso y luego se evalúa
 * </pre>
 *
 * <p>CQRS no aparece aquí porque no es "una forma de calcular": es la arquitectura que separa
 * el comando (POST) de las consultas (GET) en toda la aplicación.</p>
 */
public enum CalculationPattern {

    SINGLETON,
    FACTORY,
    STRATEGY,
    BUILDER;

    /**
     * Convierte el texto del payload en un patrón, sin importar mayúsculas/minúsculas.
     *
     * @param value texto recibido, por ejemplo {@code "strategy"}
     * @return el patrón correspondiente
     * @throws UnsupportedPatternException si el texto no coincide con ningún patrón
     */
    public static CalculationPattern from(String value) {
        return Arrays.stream(values())
                .filter(pattern -> pattern.name().equalsIgnoreCase(value == null ? "" : value.trim()))
                .findFirst()
                .orElseThrow(() -> new UnsupportedPatternException(value));
    }
}
