package com.math.api.controller;

import com.math.api.advice.GlobalExceptionHandler;
import com.math.api.advice.LoggingResponseAdvice;
import com.math.application.command.CalculateCommand;
import com.math.application.command.CalculationResult;
import com.math.application.cqrs.CommandHandler;
import com.math.domain.exception.DivisionByZeroException;
import com.math.domain.pattern.CalculationPattern;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// MockMvc "standalone": levanta solo el controlador y los advices, sin base de datos ni Redis.
@ExtendWith(MockitoExtension.class)
class CalculationControllerTest {

    @Mock
    private CommandHandler<CalculateCommand, CalculationResult> handler;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new CalculationController(handler))
                .setControllerAdvice(new GlobalExceptionHandler(), new LoggingResponseAdvice())
                .build();
    }

    @Test
    void returnsTheWrappedResultWith201() throws Exception {
        when(handler.handle(any())).thenReturn(CalculationResult.builder()
                .transactionId(1L)
                .num1(BigDecimal.TEN)
                .num2(new BigDecimal("5"))
                .operation("+")
                .pattern(CalculationPattern.STRATEGY)
                .result(new BigDecimal("15"))
                .engine("StrategyCalculationEngine")
                .trace(List.of("step"))
                .build());

        mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"num1": 10, "num2": 5, "operation": "+", "pattern": "strategy"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value(201))
                .andExpect(jsonPath("$.status").value("CREATED"))
                .andExpect(jsonPath("$.data.result").value(15))
                .andExpect(jsonPath("$.data.pattern").value("STRATEGY"))
                .andExpect(jsonPath("$.data.transactionId").value(1));

        verify(handler).handle(new CalculateCommand(BigDecimal.TEN, new BigDecimal("5"), "+", CalculationPattern.STRATEGY));
    }

    @Test
    void rejectsInvalidPayloadsWith400AndFieldErrors() throws Exception {
        mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"num2": 5, "operation": "x", "pattern": "OBSERVER"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.data.message").value("Validation failed"))
                .andExpect(jsonPath("$.data.fields.num1").value("num1 is required"))
                .andExpect(jsonPath("$.data.fields.operation").value(containsString("+ - * / % ^")))
                .andExpect(jsonPath("$.data.fields.pattern").value(containsString("SINGLETON")));

        verifyNoInteractions(handler);
    }

    @Test
    void rejectsMalformedJsonWith400() throws Exception {
        mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ not json"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.message").value("Malformed JSON request body"));
    }

    @Test
    void mapsMathErrorsTo422() throws Exception {
        when(handler.handle(any())).thenThrow(new DivisionByZeroException());

        mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"num1": 1, "num2": 0, "operation": "/", "pattern": "FACTORY"}
                                """))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value(422))
                .andExpect(jsonPath("$.data.message").value("Division by zero is not allowed"));
    }

    @Test
    void mapsUnexpectedErrorsTo500WithoutLeakingDetails() throws Exception {
        when(handler.handle(any())).thenThrow(new IllegalStateException("database password is wrong"));

        mockMvc.perform(post("/api/v1/calculations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"num1": 1, "num2": 1, "operation": "+", "pattern": "FACTORY"}
                                """))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.data.message").value("An unexpected error occurred"));
    }
}
