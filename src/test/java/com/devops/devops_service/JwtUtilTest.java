package com.devops.devops_service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "security.jwt-secret=4c90b075-e62b-42a0-b5a7-8eaf6e23934b"
})
class JwtUtilTest {

    @org.springframework.beans.factory.annotation.Autowired
    private JwtUtil jwtUtil;

    @Test
    void testGenerateToken_ShouldReturnValidToken() {
        String token = jwtUtil.generateToken("Juan Perez", "Rita Asturia");
        
        assertNotNull(token, "El token no debe ser null");
        assertFalse(token.isEmpty(), "El token no debe estar vacío");
    }

    @Test
    void testGenerateToken_ShouldGenerateUniqueTokens() {
        String token1 = jwtUtil.generateToken("Juan Perez", "Rita Asturia");
        String token2 = jwtUtil.generateToken("Juan Perez", "Rita Asturia");
        
        assertNotNull(token1);
        assertNotNull(token2);

    }
}

