package com.math.application.query;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.Instant;

/**
 * Cómo se muestra una transacción en los GET.
 *
 * <p>{@code request} y {@code response} se guardan como texto JSON; aquí se exponen como
 * {@link JsonNode} para que la respuesta HTTP muestre objetos JSON y no strings escapados.</p>
 *
 * @param id        identificador en PostgreSQL
 * @param request   payload original
 * @param response  resultado devuelto
 * @param createdAt fecha de la transacción
 */
public record TransactionView(Long id, JsonNode request, JsonNode response, Instant createdAt) {
}
