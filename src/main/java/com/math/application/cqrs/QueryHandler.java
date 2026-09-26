package com.math.application.cqrs;

/**
 * Responde una {@link Query} sin modificar el estado.
 *
 * @param <Q> tipo de consulta que atiende
 * @param <R> tipo de respuesta
 */
public interface QueryHandler<Q extends Query<R>, R> {

    /**
     * @param query consulta a responder
     * @return la respuesta
     */
    R handle(Q query);
}
