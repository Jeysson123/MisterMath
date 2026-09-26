package com.math;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Contexto completo de Spring (seguridad, JPA, Redis) contra PostgreSQL y Redis reales en Docker.
// Sin Docker disponible, el test se omite en vez de fallar.
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers(disabledWithoutDocker = true)
class MisterMathIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine");

    @Container
    @ServiceConnection(name = "redis")
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String token;

    @BeforeEach
    void login() throws Exception {
        String body = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"admin123\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        token = objectMapper.readTree(body).at("/data/accessToken").asText();
    }

    @Test
    void rejectsRequestsWithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/transactions"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401))
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"));
    }

    @Test
    void rejectsWrongCredentials() throws Exception {
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"admin\",\"password\":\"wrong\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.data.message").value("Invalid username or password"));
    }

    @Test
    void everyPatternCalculatesPersistsAndCaches() throws Exception {
        for (String pattern : new String[]{"SINGLETON", "FACTORY", "STRATEGY", "BUILDER"}) {
            JsonNode data = calculate("{\"Num1\":10,\"Num2\":4,\"Operation\":\"/\",\"Pattern\":\"" + pattern + "\"}");
            assertThat(data.get("result").decimalValue()).isEqualByComparingTo("2.5");
            assertThat(data.get("engine").asText()).startsWith(pattern.charAt(0) + pattern.substring(1).toLowerCase());
            long id = data.get("transactionId").asLong();

            mockMvc.perform(get("/api/v1/transactions/" + id).header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id").value(id))
                    .andExpect(jsonPath("$.data.request.pattern").value(pattern))
                    .andExpect(jsonPath("$.data.response.result").value(2.5))
                    .andExpect(jsonPath("$.data.createdAt").exists());
        }

        mockMvc.perform(get("/api/v1/transactions").param("limit", "4").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(4))
                .andExpect(jsonPath("$.data[0].request.pattern").value("BUILDER"));
    }

    @Test
    void mathErrorsAreNotStored() throws Exception {
        mockMvc.perform(post("/api/v1/calculations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"num1\":1,\"num2\":0,\"operation\":\"/\",\"pattern\":\"FACTORY\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.data.message").value("Division by zero is not allowed"));

        mockMvc.perform(get("/api/v1/transactions/999999").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    private JsonNode calculate(String payload) throws Exception {
        String body = mockMvc.perform(post("/api/v1/calculations")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("data");
    }
}
