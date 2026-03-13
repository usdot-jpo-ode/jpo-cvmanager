package com.trihydro.rsuinfobridge.exception;

import com.trihydro.rsuinfobridge.service.RsuService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class GlobalExceptionHandlerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RsuService rsuService;

    @Test
    void handleMethodArgumentTypeMismatchException_returnsBadRequest() throws Exception {
        // Act & Assert - pass an invalid boolean value to trigger type mismatch
        mockMvc.perform(get("/rsus")
                        .param("timDepositEnabledOnly", "notaboolean"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Type Mismatch"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void handleHttpRequestMethodNotSupportedException_returnsMethodNotAllowed() throws Exception {
        // Act & Assert - POST is not allowed on /rsus endpoint
        mockMvc.perform(post("/rsus"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.title").value("Method Not Allowed"))
                .andExpect(jsonPath("$.status").value(405))
                .andExpect(jsonPath("$.detail").value("HTTP method 'POST' is not supported for this endpoint"));
    }

    @Test
    void handleDataAccessException_returnsServiceUnavailable() throws Exception {
        // Arrange
        when(rsuService.getAll(anyBoolean()))
                .thenThrow(new DataAccessResourceFailureException("Database connection failed"));

        // Act & Assert
        mockMvc.perform(get("/rsus"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Database Error"))
                .andExpect(jsonPath("$.status").value(503))
                .andExpect(jsonPath("$.detail").value("An error occurred while accessing the database"));
    }

    @Test
    void handleDataAccessException_withDataIntegrityViolation_returnsServiceUnavailable() throws Exception {
        // Arrange - DataIntegrityViolationException is a subclass of DataAccessException
        when(rsuService.getAll(anyBoolean()))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violated"));

        // Act & Assert - handled by DataAccessException handler
        mockMvc.perform(get("/rsus"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.title").value("Database Error"))
                .andExpect(jsonPath("$.status").value(503));
    }

    @Test
    void handleGenericException_returnsInternalServerError() throws Exception {
        // Arrange
        when(rsuService.getAll(anyBoolean()))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        mockMvc.perform(get("/rsus"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal Server Error"))
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }
}
