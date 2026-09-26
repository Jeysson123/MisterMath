package com.math.application.query;

import com.math.application.cqrs.QueryHandler;
import com.math.application.port.TransactionReader;
import com.math.domain.exception.TransactionNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Responde {@link GetTransactionByIdQuery} leyendo <b>solo PostgreSQL</b>.
 *
 * <pre>
 *  GET /api/v1/transactions/{id} ─► este handler ─► TransactionReader (PostgreSQL)
 *                                                    └─ no existe ─► 404
 * </pre>
 */
@Service
@RequiredArgsConstructor
public class GetTransactionByIdQueryHandler implements QueryHandler<GetTransactionByIdQuery, TransactionView> {

    private final TransactionReader transactionReader;
    private final TransactionViewMapper mapper;

    /**
     * {@inheritDoc}
     *
     * @throws TransactionNotFoundException si el id no existe
     */
    @Override
    @Transactional(readOnly = true)
    public TransactionView handle(GetTransactionByIdQuery query) {
        return transactionReader.findById(query.id())
                .map(mapper::toView)
                .orElseThrow(() -> new TransactionNotFoundException(query.id()));
    }
}
