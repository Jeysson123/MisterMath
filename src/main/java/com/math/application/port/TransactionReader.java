package com.math.application.port;

import com.math.application.model.TransactionRecord;

import java.util.Optional;

/**
 * Puerto de <b>lectura</b> de la base de datos (lado Query de CQRS).
 */
public interface TransactionReader {

    /**
     * @param id identificador de la transacción
     * @return la transacción, o vacío si no existe
     */
    Optional<TransactionRecord> findById(Long id);
}
