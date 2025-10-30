package com.farmatodo.client_service.controller;

import com.farmatodo.client_service.dto.ClientRequestDTO;
import com.farmatodo.client_service.dto.ClientResponseDTO;
import com.farmatodo.client_service.dto.ClientUpdateDTO;
import com.farmatodo.client_service.exception.BusinessException;
import com.farmatodo.client_service.handler.GlobalExceptionHandler;
import com.farmatodo.client_service.service.ClientService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = ClientController.class,
        useDefaultFilters = false,
        includeFilters = @ComponentScan.Filter(classes = ClientController.class)
)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
        "api.key=test-api-key-12345"
})
@Import(GlobalExceptionHandler.class)
class ClientControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ClientService clientService;

    private ClientRequestDTO validClientRequest;
    private ClientResponseDTO clientResponse;

    @BeforeEach
    void setUp() {
        validClientRequest = new ClientRequestDTO(
                "John Doe",
                "john.doe@example.com",
                "+1234567890",
                "123 Main Street, City",
                "DNI",
                "12345678"
        );

        clientResponse = ClientResponseDTO.builder()
                .id(1L)
                .name("John Doe")
                .email("john.doe@example.com")
                .phone("+1234567890")
                .address("123 Main Street, City")
                .documentType("DNI")
                .documentNumber("12345678")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ==================== PING ENDPOINT TESTS ====================

    @Test
    void testPing_ShouldReturnPong() throws Exception {
        mockMvc.perform(get("/clients/ping"))
                .andExpect(status().isOk())
                .andExpect(content().string("pong"));
    }

    // ==================== CREATE CLIENT - HAPPY PATH TESTS ====================

    @Test
    void testCreateClient_ValidData_ShouldReturnCreated() throws Exception {
        when(clientService.createClient(any(ClientRequestDTO.class))).thenReturn(clientResponse);

        mockMvc.perform(post("/clients")
                        .header("Authorization", "ApiKey test-api-key-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validClientRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.phone").value("+1234567890"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        verify(clientService, times(1)).createClient(any(ClientRequestDTO.class));
    }

    // ==================== CREATE CLIENT - VALIDATION TESTS ====================

    @Test
    void testCreateClient_DuplicateEmail_ShouldReturnConflict() throws Exception {
        when(clientService.createClient(any(ClientRequestDTO.class)))
                .thenThrow(new BusinessException("Email already registered", "EMAIL_ALREADY_EXISTS", 409));

        mockMvc.perform(post("/clients")
                        .header("Authorization", "ApiKey test-api-key-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validClientRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("EMAIL_ALREADY_EXISTS"))
                .andExpect(jsonPath("$.message").value("Email already registered"));
    }

    @Test
    void testCreateClient_DuplicatePhone_ShouldReturnConflict() throws Exception {
        when(clientService.createClient(any(ClientRequestDTO.class)))
                .thenThrow(new BusinessException("Phone already registered", "PHONE_ALREADY_EXISTS", 409));

        mockMvc.perform(post("/clients")
                        .header("Authorization", "ApiKey test-api-key-12345")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validClientRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("PHONE_ALREADY_EXISTS"));
    }

    // ==================== GET CLIENT BY ID ====================

    @Test
    void testGetClientById_ExistingClient_ShouldReturnClient() throws Exception {
        when(clientService.getClientById(1L)).thenReturn(clientResponse);

        mockMvc.perform(get("/clients/1")
                        .header("Authorization", "ApiKey test-api-key-12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(clientService, times(1)).getClientById(1L);
    }

    @Test
    void testGetClientById_NonExistingClient_ShouldReturnNotFound() throws Exception {
        when(clientService.getClientById(999L))
                .thenThrow(new BusinessException("Client not found", "CLIENT_NOT_FOUND", 404));

        mockMvc.perform(get("/clients/999")
                        .header("Authorization", "ApiKey test-api-key-12345"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CLIENT_NOT_FOUND"));
    }

    // ==================== DELETE CLIENT ====================

    @Test
    void testDeleteClient_ExistingClient_ShouldReturnNoContent() throws Exception {
        doNothing().when(clientService).deleteClient(1L);

        mockMvc.perform(delete("/clients/1")
                        .header("Authorization", "ApiKey test-api-key-12345"))
                .andExpect(status().isNoContent());

        verify(clientService, times(1)).deleteClient(1L);
    }

    @Test
    void testDeleteClient_NonExistingClient_ShouldReturnNotFound() throws Exception {
        doThrow(new BusinessException("Client not found", "CLIENT_NOT_FOUND", 404))
                .when(clientService).deleteClient(999L);

        mockMvc.perform(delete("/clients/999")
                        .header("Authorization", "ApiKey test-api-key-12345"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("CLIENT_NOT_FOUND"));
    }

    // ==================== API KEY AUTH ====================

    @Test
    void testCreateClient_MissingApiKey_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/clients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validClientRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));

        verify(clientService, never()).createClient(any());
    }

    @Test
    void testCreateClient_InvalidApiKey_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/clients")
                        .header("Authorization", "ApiKey wrong-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validClientRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("UNAUTHORIZED"));

        verify(clientService, never()).createClient(any());
    }
}
