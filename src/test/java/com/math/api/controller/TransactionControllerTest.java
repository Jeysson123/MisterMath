package com.math.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.math.api.advice.GlobalExceptionHandler;
import com.math.api.advice.LoggingResponseAdvice;
import com.math.application.cqrs.QueryHandler;
import com.math.application.query.GetCachedTransactionsQuery;
import com.math.application.query.GetTransactionByIdQuery;
import com.math.application.query.TransactionView;
import com.math.domain.exception.TransactionNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private QueryHandler<GetCachedTransactionsQuery, List<TransactionView>> cachedHandler;

    @Mock
    private QueryHandler<GetTransactionByIdQuery, TransactionView> byIdHandler;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TransactionController(cachedHandler, byIdHandler))
                .setControllerAdvice(new GlobalExceptionHandler(), new LoggingResponseAdvice())
                .build();
    }

    @Test
    void listsCachedTransactions() throws Exception {
        when(cachedHandler.handle(new GetCachedTransactionsQuery(20))).thenReturn(List.of(view(2L), view(1L)));

        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.status").value("OK"))
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(2))
                .andExpect(jsonPath("$.data[0].request.num1").value(10));
    }

    @Test
    void rejectsLimitsOutOfRange() throws Exception {
        mockMvc.perform(get("/api/v1/transactions").param("limit", "500"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.fields.limit").exists());

        verifyNoInteractions(cachedHandler);
    }

    @Test
    void findsOneTransactionById() throws Exception {
        when(byIdHandler.handle(new GetTransactionByIdQuery(1L))).thenReturn(view(1L));

        mockMvc.perform(get("/api/v1/transactions/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.response.result").value(15));
    }

    @Test
    void returns404ForUnknownIds() throws Exception {
        when(byIdHandler.handle(new GetTransactionByIdQuery(9L))).thenThrow(new TransactionNotFoundException(9L));

        mockMvc.perform(get("/api/v1/transactions/9"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("NOT_FOUND"))
                .andExpect(jsonPath("$.data.message").value("Transaction 9 was not found"));
    }

    @Test
    void returns400ForNonNumericIds() throws Exception {
        mockMvc.perform(get("/api/v1/transactions/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.data.message").value("Parameter 'id' has an invalid value: abc"));
    }

    private TransactionView view(Long id) throws Exception {
        return new TransactionView(id,
                objectMapper.readTree("{\"num1\":10}"),
                objectMapper.readTree("{\"result\":15}"),
                null);
    }
}
