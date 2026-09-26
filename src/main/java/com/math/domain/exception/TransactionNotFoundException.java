package com.math.domain.exception;

/**
 * Se lanza cuando {@code GET /api/v1/transactions/{id}} no encuentra la transacción en PostgreSQL.
 *
 * <p>No hereda de {@link MathDomainException} porque no es un error matemático: el
 * {@code GlobalExceptionHandler} la traduce a {@code 404 NOT_FOUND}.</p>
 */
public class TransactionNotFoundException extends RuntimeException {

    /**
     * @param id identificador buscado
     */
    public TransactionNotFoundException(Long id) {
        super("Transaction " + id + " was not found");
    }
}
