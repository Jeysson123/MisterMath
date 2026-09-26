package com.math.application.model;

import java.time.Instant;

/**
 * Una transacción guardada: el request, el response y la fecha.
 *
 * <p>Es el modelo que viaja entre la aplicación y los adaptadores (PostgreSQL y Redis). Así la
 * capa de aplicación no conoce la entidad JPA ({@code TransactionEntity}).</p>
 *
 * @param id        identificador generado por PostgreSQL
 * @param request   payload recibido, en JSON
 * @param response  resultado devuelto, en JSON
 * @param createdAt fecha y hora en que se guardó
 */
public record TransactionRecord(Long id, String request, String response, Instant createdAt) {
}
