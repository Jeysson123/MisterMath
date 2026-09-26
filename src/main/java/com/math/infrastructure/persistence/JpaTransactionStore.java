package com.math.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.math.application.model.TransactionRecord;
import com.math.application.port.TransactionReader;
import com.math.application.port.TransactionWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Adaptador de PostgreSQL: implementa los puertos {@link TransactionWriter} y
 * {@link TransactionReader} usando JPA.
 *
 * <p><b>D - Dependency Inversion:</b> la aplicación define los puertos y esta clase los cumple;
 * la dependencia apunta de la infraestructura hacia la aplicación, nunca al revés.</p>
 *
 * <pre>
 *  CalculateCommandHandler ──► TransactionWriter ◄── JpaTransactionStore ──► PostgreSQL
 *  GetTransactionById...   ──► TransactionReader ◄──┘
 * </pre>
 */
@Repository
@RequiredArgsConstructor
public class JpaTransactionStore implements TransactionWriter, TransactionReader {

    private final TransactionJpaRepository repository;
    private final ObjectMapper objectMapper;

    @Override
    public TransactionRecord save(Object request, Object response) {
        TransactionEntity entity = TransactionEntity.builder()
                .request(toJson(request))
                .response(toJson(response))
                .build();
        return toRecord(repository.save(entity));
    }

    @Override
    public Optional<TransactionRecord> findById(Long id) {
        return repository.findById(id).map(this::toRecord);
    }

    /**
     * @param value objeto a serializar
     * @return el objeto en JSON
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Could not serialize " + value.getClass().getSimpleName(), e);
        }
    }

    /**
     * @param entity fila de la tabla
     * @return el modelo que entiende la capa de aplicación
     */
    private TransactionRecord toRecord(TransactionEntity entity) {
        return new TransactionRecord(entity.getId(), entity.getRequest(), entity.getResponse(), entity.getCreatedAt());
    }
}
