package com.math.api.controller;

import com.math.api.dto.ApiResponse;
import com.math.application.cqrs.QueryHandler;
import com.math.application.query.GetCachedTransactionsQuery;
import com.math.application.query.GetTransactionByIdQuery;
import com.math.application.query.TransactionView;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lado <b>Query</b> de CQRS: solo lectura.
 *
 * <pre>
 *  GET /api/v1/transactions?limit=20 ─► GetCachedTransactionsQueryHandler ─► Redis
 *  GET /api/v1/transactions/{id}     ─► GetTransactionByIdQueryHandler    ─► PostgreSQL
 * </pre>
 *
 * <p>Las anotaciones {@code @Min}, {@code @Max} y {@code @Positive} sobre los parámetros las
 * valida Spring directamente (validación de métodos); si fallan → 400.</p>
 */
@RestController
@RequestMapping("/api/v1/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final QueryHandler<GetCachedTransactionsQuery, List<TransactionView>> cachedTransactionsHandler;
    private final QueryHandler<GetTransactionByIdQuery, TransactionView> transactionByIdHandler;

    /**
     * Transacciones recientes guardadas en Redis.
     *
     * @param limit cuántas devolver (1 a 100, por defecto 20)
     * @return lista, la más reciente primero
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<TransactionView>>> findCached(
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int limit) {
        return ResponseEntity.ok(ApiResponse.ok(cachedTransactionsHandler.handle(new GetCachedTransactionsQuery(limit))));
    }

    /**
     * Una transacción desde PostgreSQL.
     *
     * @param id identificador (positivo)
     * @return la transacción, o 404 si no existe
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TransactionView>> findById(@PathVariable @Positive Long id) {
        return ResponseEntity.ok(ApiResponse.ok(transactionByIdHandler.handle(new GetTransactionByIdQuery(id))));
    }
}
