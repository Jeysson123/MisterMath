package com.math.application.cqrs;

/**
 * <b>CQRS — Query.</b> Marca una pregunta al sistema que <b>no cambia</b> nada.
 *
 * @param <R> tipo de la respuesta que produce la consulta (ayuda a leer el código: cada query
 *            "declara" qué devuelve)
 */
public interface Query<R> {
}
