package com.math.application.cqrs;

/**
 * <b>CQRS — Command.</b> Marca una intención de <b>cambiar</b> el estado del sistema.
 *
 * <p>CQRS (Command Query Responsibility Segregation) separa en dos caminos distintos:</p>
 * <pre>
 *  ESCRIBIR ─► Command ─► CommandHandler ─► PostgreSQL + Redis   (POST /calculations)
 *  LEER     ─► Query   ─► QueryHandler   ─► Redis ó PostgreSQL   (GET  /transactions...)
 * </pre>
 *
 * <p>Un comando es un objeto inmutable con los datos necesarios; no sabe cómo se ejecuta.</p>
 */
public interface Command {
}
