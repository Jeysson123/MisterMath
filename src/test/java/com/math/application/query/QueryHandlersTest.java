package com.math.application.query;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.math.application.model.TransactionRecord;
import com.math.application.port.TransactionCacheReader;
import com.math.application.port.TransactionReader;
import com.math.domain.exception.TransactionNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueryHandlersTest {

    private static final Instant NOW = Instant.parse("2026-09-25T12:00:00Z");

    @Mock
    private TransactionCacheReader cacheReader;

    @Mock
    private TransactionReader transactionReader;

    private final TransactionViewMapper mapper = new TransactionViewMapper(new ObjectMapper());

    @Test
    void cachedQueryReadsRedisAndExposesJson() {
        when(cacheReader.findLatest(2)).thenReturn(List.of(
                new TransactionRecord(2L, "{\"num1\":1}", "{\"result\":2}", NOW),
                new TransactionRecord(1L, "{\"num1\":3}", "{\"result\":4}", NOW)));

        List<TransactionView> views = new GetCachedTransactionsQueryHandler(cacheReader, mapper)
                .handle(new GetCachedTransactionsQuery(2));

        assertThat(views).extracting(TransactionView::id).containsExactly(2L, 1L);
        assertThat(views.get(0).request().get("num1").asInt()).isEqualTo(1);
        assertThat(views.get(0).response().get("result").asInt()).isEqualTo(2);
        assertThat(views.get(0).createdAt()).isEqualTo(NOW);
    }

    @Test
    void byIdQueryReadsPostgres() {
        when(transactionReader.findById(5L)).thenReturn(Optional.of(new TransactionRecord(5L, "{}", "{}", NOW)));

        TransactionView view = new GetTransactionByIdQueryHandler(transactionReader, mapper)
                .handle(new GetTransactionByIdQuery(5L));

        assertThat(view.id()).isEqualTo(5L);
    }

    @Test
    void byIdQueryFailsWhenMissing() {
        when(transactionReader.findById(99L)).thenReturn(Optional.empty());
        GetTransactionByIdQueryHandler handler = new GetTransactionByIdQueryHandler(transactionReader, mapper);

        assertThatThrownBy(() -> handler.handle(new GetTransactionByIdQuery(99L)))
                .isInstanceOf(TransactionNotFoundException.class)
                .hasMessage("Transaction 99 was not found");
    }

    @Test
    void mapperRejectsCorruptJson() {
        TransactionRecord corrupt = new TransactionRecord(1L, "not json", "{}", NOW);

        assertThatThrownBy(() -> mapper.toView(corrupt)).isInstanceOf(IllegalStateException.class);
    }
}
