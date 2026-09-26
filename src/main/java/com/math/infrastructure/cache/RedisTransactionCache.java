package com.math.infrastructure.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.math.application.model.TransactionRecord;
import com.math.application.port.TransactionCacheReader;
import com.math.application.port.TransactionCacheWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

/**
 * Adaptador de Redis: guarda las transacciones recientes en una <b>lista</b>.
 *
 * <pre>
 *  push(tx)       LPUSH mistermath:transactions "{json}"   ← la más nueva queda al inicio
 *                 LTRIM mistermath:transactions 0 99       ← se conservan solo maxSize
 *  findLatest(n)  LRANGE mistermath:transactions 0 n-1
 * </pre>
 *
 * <p>PostgreSQL es la fuente de verdad. Si Redis falla al escribir, se registra un
 * {@code WARN} y el cálculo sigue respondiendo bien: la caché es un extra, no un requisito.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisTransactionCache implements TransactionCacheWriter, TransactionCacheReader {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final CacheProperties properties;

    @Override
    public void push(TransactionRecord transaction) {
        try {
            redisTemplate.opsForList().leftPush(properties.transactionsKey(), objectMapper.writeValueAsString(transaction));
            redisTemplate.opsForList().trim(properties.transactionsKey(), 0, properties.maxSize() - 1L);
        } catch (JsonProcessingException | RuntimeException e) {
            log.warn("Transaction {} could not be cached in Redis: {}", transaction.id(), e.getMessage());
        }
    }

    @Override
    public List<TransactionRecord> findLatest(int limit) {
        List<String> values = redisTemplate.opsForList().range(properties.transactionsKey(), 0, limit - 1L);
        if (values == null) {
            return List.of();
        }
        return values.stream()
                .map(this::fromJson)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * @param json una entrada de la lista
     * @return la transacción, o {@code null} si la entrada está corrupta (se ignora)
     */
    private TransactionRecord fromJson(String json) {
        try {
            return objectMapper.readValue(json, TransactionRecord.class);
        } catch (JsonProcessingException e) {
            log.warn("Skipping unreadable cache entry: {}", e.getMessage());
            return null;
        }
    }
}
