package com.math.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Map;

/**
 * Contenido de {@code data} cuando la respuesta es un error.
 *
 * <pre>
 *  {
 *    "error":   "Bad Request",
 *    "message": "Validation failed",
 *    "fields":  { "num1": "num1 is required" }   ← solo en errores de validación
 *  }
 * </pre>
 *
 * @param error   nombre corto del error
 * @param message explicación para el cliente
 * @param fields  errores por campo; se omite del JSON cuando está vacío
 */
public record ApiError(
        String error,
        String message,
        @JsonInclude(JsonInclude.Include.NON_EMPTY) Map<String, String> fields) {

    /**
     * Error sin detalle por campo.
     *
     * @param error   nombre corto del error
     * @param message explicación
     * @return el error
     */
    public static ApiError of(String error, String message) {
        return new ApiError(error, message, Map.of());
    }
}
