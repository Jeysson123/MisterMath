package com.math.application.port;

import com.math.application.model.TransactionRecord;

import java.util.List;

/**
 * Puerto de <b>lectura</b> de la caché (Redis).
 */
public interface TransactionCacheReader {

    /**
     * @param limit cuántas transacciones devolver como máximo
     * @return las más recientes primero
     */
    List<TransactionRecord> findLatest(int limit);
}
