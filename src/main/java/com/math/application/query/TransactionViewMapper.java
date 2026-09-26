package com.math.application.query;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.math.application.model.TransactionRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Convierte un {@link TransactionRecord} en un {@link TransactionView}.
 *
 * <p><b>S - Single Responsibility:</b> la conversión vive en una sola clase y la reutilizan las
 * dos consultas (Redis y PostgreSQL).</p>
 */
@Component
@RequiredArgsConstructor
public class TransactionViewMapper {

    private final ObjectMapper objectMapper;

    /**
     * @param transaction transacción guardada
     * @return la vista con request/response como JSON real
     * @throws IllegalStateException si lo guardado no es JSON válido
     */
    public TransactionView toView(TransactionRecord transaction) {
        try {
            return new TransactionView(
                    transaction.id(),
                    objectMapper.readTree(transaction.request()),
                    objectMapper.readTree(transaction.response()),
                    transaction.createdAt());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Stored transaction " + transaction.id() + " is not valid JSON", e);
        }
    }
}
