package com.math.domain.exception;

import com.math.domain.pattern.CalculationPattern;

import java.util.Arrays;

/**
 * Se lanza cuando se pide un patrón que no existe o que no tiene un motor registrado.
 */
public class UnsupportedPatternException extends MathDomainException {

    /**
     * @param pattern patrón rechazado
     */
    public UnsupportedPatternException(String pattern) {
        super("Unsupported pattern '" + pattern + "'. Supported: "
                + Arrays.toString(CalculationPattern.values()));
    }
}
