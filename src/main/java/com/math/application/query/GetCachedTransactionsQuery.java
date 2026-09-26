package com.math.application.query;

import com.math.application.cqrs.Query;

import java.util.List;

/**
 * Consulta "dame las últimas {@code limit} transacciones guardadas en Redis".
 *
 * @param limit cantidad máxima a devolver
 */
public record GetCachedTransactionsQuery(int limit) implements Query<List<TransactionView>> {
}
