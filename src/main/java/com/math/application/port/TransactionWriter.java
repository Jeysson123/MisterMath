package com.math.application.port;

import com.math.application.model.TransactionRecord;

/**
 * Puerto de <b>escritura</b> en la base de datos.
 *
 * <p><b>D - Dependency Inversion:</b> el command handler depende de esta interfaz, no de JPA ni
 * de PostgreSQL. Cambiar a MySQL solo exige otra implementación.</p>
 *
 * <p><b>I - Interface Segregation:</b> escribir y leer son interfaces separadas
 * ({@link TransactionReader}); el comando no ve métodos de lectura y las consultas no ven
 * {@code save}.</p>
 */
public interface TransactionWriter {

    /**
     * Guarda request y response serializados como JSON junto con la fecha actual.
     *
     * @param request  objeto de entrada (se serializa a JSON)
     * @param response objeto de salida (se serializa a JSON)
     * @return la transacción guardada, ya con su {@code id}
     */
    TransactionRecord save(Object request, Object response);
}
