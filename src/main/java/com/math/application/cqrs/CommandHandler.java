package com.math.application.cqrs;

/**
 * Ejecuta un {@link Command}.
 *
 * <p><b>I - Interface Segregation:</b> quien solo escribe implementa esta interfaz; quien solo lee
 * implementa {@link QueryHandler}. Ninguna clase se ve obligada a tener métodos que no usa.</p>
 *
 * @param <C> tipo de comando que atiende
 * @param <R> tipo de resultado
 */
public interface CommandHandler<C extends Command, R> {

    /**
     * @param command comando a ejecutar
     * @return resultado de la ejecución
     */
    R handle(C command);
}
