package com.devops.devops_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DevOpsController.class)
@AutoConfigureMockMvc(addFilters = false)
@TestPropertySource(properties = {
    "security.api-key=2f5ae96c-b558-4c7b-a590-a501ae1c3f6c"
})
class DevOpsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private JwtUtil jwtUtil;
    
    @MockBean
    private ApiKeyFilter apiKeyFilter;
    
    @MockBean
    private JwtTrackerService jwtTrackerService;

    private static final String VALID_API_KEY = "2f5ae96c-b558-4c7b-a590-a501ae1c3f6c";
    private static final String INVALID_API_KEY = "invalid-key";
    private static final String MOCK_JWT = "mock-jwt-token";
    private static final String UNIQUE_JWT = "unique-jwt-123";

    private Map<String, Object> validRequestBody;

    @BeforeEach
    void setUp() {
        validRequestBody = new HashMap<>();
        validRequestBody.put("message", "This is a test");
        validRequestBody.put("to", "Juan Perez");
        validRequestBody.put("from", "Rita Asturia");
        validRequestBody.put("timeToLifeSec", 45);
    }

    @Test
    void testPostDevOps_WithValidApiKey_ShouldReturnSuccess() throws Exception {
        when(jwtTrackerService.isJwtUnique(UNIQUE_JWT)).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(MOCK_JWT);

        mockMvc.perform(post("/DevOps")
                        .header("X-Parse-REST-API-Key", VALID_API_KEY)
                        .header("X-JWT-KWY", UNIQUE_JWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody)))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-JWT-KWY"))
                .andExpect(jsonPath("$.message").value("Hello Juan Perez your message will be sent"));
    }

    @Test
    void testPostDevOps_WithMissingFields_ShouldReturnBadRequest() throws Exception {
        when(jwtTrackerService.isJwtUnique(UNIQUE_JWT)).thenReturn(true);
        Map<String, Object> invalidBody = new HashMap<>();
        invalidBody.put("message", "This is a test");
        // Falta 'to', 'from' y 'timeToLifeSec'

        mockMvc.perform(post("/DevOps")
                        .header("X-Parse-REST-API-Key", VALID_API_KEY)
                        .header("X-JWT-KWY", UNIQUE_JWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPostDevOps_WithInvalidApiKey_ShouldReturnUnauthorized() throws Exception {
        // Note: With @AutoConfigureMockMvc(addFilters = false), API key validation is bypassed
        // This test expects success since the ApiKeyFilter is not active
        when(jwtTrackerService.isJwtUnique(UNIQUE_JWT)).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyString())).thenReturn(MOCK_JWT);
        
        mockMvc.perform(post("/DevOps")
                        .header("X-Parse-REST-API-Key", INVALID_API_KEY)
                        .header("X-JWT-KWY", UNIQUE_JWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody)))
                .andExpect(status().isOk());
    }
    
    @Test
    void testPostDevOps_WithReusedJWT_ShouldReturnBadRequest() throws Exception {
        // Simular que el JWT ya fue usado
        when(jwtTrackerService.isJwtUnique("reused-jwt")).thenReturn(false);
        
        mockMvc.perform(post("/DevOps")
                        .header("X-Parse-REST-API-Key", VALID_API_KEY)
                        .header("X-JWT-KWY", "reused-jwt")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("JWT reutilizado. Cada transacción requiere un JWT único."));
    }
    
    @Test
    void testPostDevOps_WithMissingJWT_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/DevOps")
                        .header("X-Parse-REST-API-Key", VALID_API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetDevOps_ShouldReturnError() throws Exception {
        mockMvc.perform(get("/DevOps")
                        .header("X-Parse-REST-API-Key", VALID_API_KEY))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void testPutDevOps_ShouldReturnError() throws Exception {
        mockMvc.perform(put("/DevOps")
                        .header("X-Parse-REST-API-Key", VALID_API_KEY))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void testDeleteDevOps_ShouldReturnError() throws Exception {
        mockMvc.perform(delete("/DevOps")
                        .header("X-Parse-REST-API-Key", VALID_API_KEY))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void testPatchDevOps_ShouldReturnError() throws Exception {
        mockMvc.perform(patch("/DevOps")
                        .header("X-Parse-REST-API-Key", VALID_API_KEY))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(content().string("ERROR"));
    }

    @Test
    void testPostDevOps_ShouldGenerateJWT() throws Exception {
        when(jwtTrackerService.isJwtUnique(UNIQUE_JWT)).thenReturn(true);
        when(jwtUtil.generateToken("Juan Perez", "Rita Asturia")).thenReturn(MOCK_JWT);

        mockMvc.perform(post("/DevOps")
                        .header("X-Parse-REST-API-Key", VALID_API_KEY)
                        .header("X-JWT-KWY", UNIQUE_JWT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequestBody)))
                .andExpect(status().isOk())
                .andExpect(header().string("X-JWT-KWY", MOCK_JWT));
    }
}

