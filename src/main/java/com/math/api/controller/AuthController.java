package com.math.api.controller;

import com.math.api.dto.ApiResponse;
import com.math.api.dto.LoginRequest;
import com.math.api.dto.TokenResponse;
import com.math.infrastructure.security.AuthenticationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoint público para obtener el JWT.
 *
 * <pre>
 *  POST /api/v1/auth/login  { "username": "admin", "password": "admin123" }
 *  → 200 { code, status, data: { accessToken, tokenType, expiresIn } }
 * </pre>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    /**
     * @param request credenciales validadas
     * @return el token dentro del wrapper
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<TokenResponse>> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(ApiResponse.ok(authenticationService.login(request)));
    }
}
