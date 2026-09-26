package com.math.infrastructure.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.math.application.model.TransactionRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisTransactionCacheTest {

    private static final String KEY = "mistermath:transactions";
    private static final TransactionRecord RECORD =
            new TransactionRecord(1L, "{}", "{}", Instant.parse("2026-09-25T12:00:00Z"));

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private ListOperations<String, String> listOperations;

    private final ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    private RedisTransactionCache cache;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        cache = new RedisTransactionCache(redisTemplate, objectMapper, new CacheProperties(KEY, 100));
    }

    @Test
    void pushesToTheHeadAndTrimsTheList() throws Exception {
        cache.push(RECORD);

        verify(listOperations).leftPush(KEY, objectMapper.writeValueAsString(RECORD));
        verify(listOperations).trim(KEY, 0, 99);
    }

    // Redis caído no rompe el cálculo: PostgreSQL ya guardó la transacción.
    @Test
    void swallowsRedisFailuresOnWrite() {
        when(listOperations.leftPush(anyString(), anyString())).thenThrow(new RedisConnectionFailureException("down"));

        assertThatCode(() -> cache.push(RECORD)).doesNotThrowAnyException();
    }

    @Test
    void readsTheLatestAndSkipsCorruptEntries() throws Exception {
        when(listOperations.range(KEY, 0, 1)).thenReturn(List.of(objectMapper.writeValueAsString(RECORD), "garbage"));

        assertThat(cache.findLatest(2)).containsExactly(RECORD);
    }

    @Test
    void returnsEmptyWhenTheKeyDoesNotExist() {
        when(listOperations.range(KEY, 0, 9)).thenReturn(null);

        assertThat(cache.findLatest(10)).isEmpty();
    }
}
