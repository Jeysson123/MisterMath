package com.math.infrastructure.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.math.application.model.TransactionRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JpaTransactionStoreTest {

    private static final Instant NOW = Instant.parse("2026-09-25T12:00:00Z");

    @Mock
    private TransactionJpaRepository repository;

    @Test
    void savesRequestAndResponseAsJson() {
        when(repository.save(any(TransactionEntity.class))).thenAnswer(invocation -> {
            TransactionEntity entity = invocation.getArgument(0);
            entity.setId(1L);
            entity.setCreatedAt(NOW);
            return entity;
        });
        JpaTransactionStore store = new JpaTransactionStore(repository, new ObjectMapper());

        TransactionRecord saved = store.save(Map.of("num1", 1), Map.of("result", 2));

        assertThat(saved).isEqualTo(new TransactionRecord(1L, "{\"num1\":1}", "{\"result\":2}", NOW));
    }

    @Test
    void findsById() {
        when(repository.findById(3L)).thenReturn(Optional.of(new TransactionEntity(3L, "{}", "{}", NOW)));
        JpaTransactionStore store = new JpaTransactionStore(repository, new ObjectMapper());

        assertThat(store.findById(3L)).contains(new TransactionRecord(3L, "{}", "{}", NOW));
        assertThat(store.findById(4L)).isEmpty();
    }
}
