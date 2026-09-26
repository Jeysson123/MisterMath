package com.math.domain.exception;

import com.math.domain.operation.OperationType;

/**
 * Se lanza cuando llega un símbolo que no está en {@link OperationType}.
 *
 * <p>Normalmente la validación del request lo frena antes; esta excepción es la red de
 * seguridad si alguien usa el dominio sin pasar por el controlador.</p>
 */
public class UnsupportedOperationSymbolException extends MathDomainException {

    /**
     * @param symbol símbolo rechazado
     */
    public UnsupportedOperationSymbolException(String symbol) {
        super("Unsupported operation '" + symbol + "'. Supported: " + OperationType.symbols());
    }
}
