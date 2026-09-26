package com.math.api.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

/**
 * <b>Wrapper genérico</b> de todas las respuestas de la API.
 *
 * <pre>
 *  {
 *    "code":   201,          ← código HTTP numérico
 *    "status": "CREATED",    ← nombre del código HTTP
 *    "data":   { ... }       ← T: el resultado, o un {@link ApiError} si algo falló
 *  }
 * </pre>
 *
 * <p>El genérico {@code <T>} permite reutilizar la misma forma para cualquier contenido:
 * {@code ApiResponse<CalculationResult>}, {@code ApiResponse<List<TransactionView>>},
 * {@code ApiResponse<ApiError>}...</p>
 *
 * <p>El constructor es privado: se crea con los métodos de fábrica {@link #ok} y {@link #of},
 * que garantizan que {@code code} y {@code status} siempre coincidan.</p>
 *
 * @param <T> tipo del contenido de {@code data}
 */
@Getter
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {

    private final int code;
    private final String status;
    private final T data;

    /**
     * Respuesta {@code 200 OK}.
     *
     * @param data contenido
     * @param <T>  tipo del contenido
     * @return el wrapper
     */
    public static <T> ApiResponse<T> ok(T data) {
        return of(HttpStatus.OK, data);
    }

    /**
     * Respuesta con cualquier código HTTP.
     *
     * @param status código HTTP
     * @param data   contenido
     * @param <T>    tipo del contenido
     * @return el wrapper
     */
    public static <T> ApiResponse<T> of(HttpStatus status, T data) {
        return new ApiResponse<>(status.value(), status.name(), data);
    }
}
