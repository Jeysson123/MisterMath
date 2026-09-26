package com.math.api.controller;

import com.math.api.dto.ApiResponse;
import com.math.api.dto.CalculationRequest;
import com.math.application.command.CalculateCommand;
import com.math.application.command.CalculationResult;
import com.math.application.cqrs.CommandHandler;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lado <b>Command</b> de CQRS: el único endpoint que escribe.
 *
 * <pre>
 *  POST /api/v1/calculations
 *     │ @Valid           → 400 si el payload no cumple
 *     │ toCommand()      → CalculateCommand
 *     │ commandHandler   → el pattern decide el motor
 *     ▼
 *  201 { code: 201, status: "CREATED", data: CalculationResult }
 * </pre>
 *
 * <p><b>S:</b> el controlador solo traduce HTTP ↔ comando. <b>D:</b> depende de la interfaz
 * {@link CommandHandler}; Spring encuentra la implementación por sus tipos genéricos.</p>
 */
@RestController
@RequestMapping("/api/v1/calculations")
@RequiredArgsConstructor
public class CalculationController {

    private final CommandHandler<CalculateCommand, CalculationResult> calculateHandler;

    /**
     * Ejecuta la operación con el patrón elegido y guarda la transacción.
     *
     * @param request payload validado
     * @return el resultado dentro del wrapper, con código 201
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CalculationResult>> calculate(@Valid @RequestBody CalculationRequest request) {
        CalculationResult result = calculateHandler.handle(request.toCommand());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.of(HttpStatus.CREATED, result));
    }
}
