package com.math.application.query;

import com.math.application.cqrs.QueryHandler;
import com.math.application.port.TransactionCacheReader;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Responde {@link GetCachedTransactionsQuery} leyendo <b>solo Redis</b>.
 *
 * <pre>
 *  GET /api/v1/transactions?limit=20 ─► este handler ─► TransactionCacheReader (Redis)
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class GetCachedTransactionsQueryHandler implements QueryHandler<GetCachedTransactionsQuery, List<TransactionView>> {

    private final TransactionCacheReader cacheReader;
    private final TransactionViewMapper mapper;

    @Override
    public List<TransactionView> handle(GetCachedTransactionsQuery query) {
        return cacheReader.findLatest(query.limit()).stream()
                .map(mapper::toView)
                .toList();
    }
}
