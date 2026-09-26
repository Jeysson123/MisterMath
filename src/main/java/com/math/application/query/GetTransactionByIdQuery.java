package com.math.application.query;

import com.math.application.cqrs.Query;

/**
 * Consulta "dame la transacción {@code id} desde PostgreSQL".
 *
 * @param id identificador de la transacción
 */
public record GetTransactionByIdQuery(Long id) implements Query<TransactionView> {
}
