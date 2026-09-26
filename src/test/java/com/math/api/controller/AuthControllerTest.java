package com.math.api.controller;

import com.math.api.advice.GlobalExceptionHandler;
import com.math.api.advice.LoggingResponseAdvice;
import com.math.infrastructure.security.AuthenticationService;
import com.math.infrastructure.security.TokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenProvider tokenProvider;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        AuthenticationService service = new AuthenticationService(authenticationManager, tokenProvider);
        mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(service))
                .setControllerAdvice(new GlobalExceptionHandler(), new LoggingResponseAdvice())
                .build();
    }

    @Test
    void returnsATokenForValidCredentials() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenReturn(new UsernamePasswordAuthenticationToken("admin", null, List.of()));
        when(tokenProvider.generate("admin")).thenReturn("jwt-token");
        when(tokenProvider.expirationSeconds()).thenReturn(3600L);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").value("jwt-token"))
                .andExpect(jsonPath("$.data.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.data.expiresIn").value(3600));
    }

    @Test
    void returns401ForBadCredentials() throws Exception {
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"nope\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.data.message").value("Invalid username or password"));
    }

    @Test
    void validatesTheLoginPayload() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.fields.username").value("username is required"))
                .andExpect(jsonPath("$.data.fields.password").value("password is required"));
    }
}
