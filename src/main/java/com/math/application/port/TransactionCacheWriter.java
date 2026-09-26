package com.math.application.port;

import com.math.application.model.TransactionRecord;

/**
 * Puerto de <b>escritura</b> en la caché (Redis).
 */
public interface TransactionCacheWriter {

    /**
     * Agrega la transacción al inicio de la lista de recientes.
     *
     * @param transaction transacción ya guardada en la base de datos
     */
    void push(TransactionRecord transaction);
}
