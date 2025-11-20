package com.devops.devops_service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTrackerServiceTest {

    private JwtTrackerService jwtTrackerService;

    @BeforeEach
    void setUp() {
        jwtTrackerService = new JwtTrackerService();
    }

    @Test
    void testIsJwtUnique_WithNewJwt_ShouldReturnTrue() {
        // Arrange
        String jwt = "unique-jwt-token-123";

        // Act
        boolean result = jwtTrackerService.isJwtUnique(jwt);

        // Assert
        assertTrue(result, "Un JWT nuevo debe retornar true");
    }

    @Test
    void testIsJwtUnique_WithReusedJwt_ShouldReturnFalse() {
        // Arrange
        String jwt = "reused-jwt-token-456";
        jwtTrackerService.isJwtUnique(jwt); // Primera vez - se guarda

        // Act
        boolean result = jwtTrackerService.isJwtUnique(jwt); // Segunda vez - reutilizado

        // Assert
        assertFalse(result, "Un JWT reutilizado debe retornar false");
    }

    @Test
    void testIsJwtUnique_WithNullJwt_ShouldReturnFalse() {
        // Act
        boolean result = jwtTrackerService.isJwtUnique(null);

        // Assert
        assertFalse(result, "Un JWT null debe retornar false");
    }

    @Test
    void testIsJwtUnique_WithEmptyJwt_ShouldReturnFalse() {
        // Act
        boolean result = jwtTrackerService.isJwtUnique("");

        // Assert
        assertFalse(result, "Un JWT vacío debe retornar false");
    }

    @Test
    void testIsJwtUnique_WithWhitespaceJwt_ShouldReturnFalse() {
        // Act
        boolean result = jwtTrackerService.isJwtUnique("   ");

        // Assert
        assertFalse(result, "Un JWT solo con espacios debe retornar false");
    }

    @Test
    void testGetUsedJwtsCount_InitiallyZero() {
        // Act
        int count = jwtTrackerService.getUsedJwtsCount();

        // Assert
        assertEquals(0, count, "El contador inicial debe ser 0");
    }

    @Test
    void testGetUsedJwtsCount_AfterAddingJwts() {
        // Arrange
        jwtTrackerService.isJwtUnique("jwt-1");
        jwtTrackerService.isJwtUnique("jwt-2");
        jwtTrackerService.isJwtUnique("jwt-3");

        // Act
        int count = jwtTrackerService.getUsedJwtsCount();

        // Assert
        assertEquals(3, count, "El contador debe ser 3 después de agregar 3 JWTs únicos");
    }

    @Test
    void testGetUsedJwtsCount_DoesNotCountDuplicates() {
        // Arrange
        jwtTrackerService.isJwtUnique("jwt-1");
        jwtTrackerService.isJwtUnique("jwt-1"); // Duplicado
        jwtTrackerService.isJwtUnique("jwt-2");
        jwtTrackerService.isJwtUnique("jwt-2"); // Duplicado

        // Act
        int count = jwtTrackerService.getUsedJwtsCount();

        // Assert
        assertEquals(2, count, "El contador debe ser 2, no cuenta duplicados");
    }

    @Test
    void testClearOldJwts_WhenBelowThreshold_ShouldNotClear() {
        // Arrange
        for (int i = 0; i < 100; i++) {
            jwtTrackerService.isJwtUnique("jwt-" + i);
        }
        int countBefore = jwtTrackerService.getUsedJwtsCount();

        // Act
        jwtTrackerService.clearOldJwts();
        int countAfter = jwtTrackerService.getUsedJwtsCount();

        // Assert
        assertEquals(100, countBefore, "Debe tener 100 JWTs antes de limpiar");
        assertEquals(100, countAfter, "No debe limpiar cuando está por debajo del threshold");
    }

    @Test
    void testClearOldJwts_WhenAboveThreshold_ShouldClear() {
        // Arrange - Agregar más de 10000 JWTs
        for (int i = 0; i < 10001; i++) {
            jwtTrackerService.isJwtUnique("jwt-" + i);
        }
        int countBefore = jwtTrackerService.getUsedJwtsCount();

        // Act
        jwtTrackerService.clearOldJwts();
        int countAfter = jwtTrackerService.getUsedJwtsCount();

        // Assert
        assertEquals(10001, countBefore, "Debe tener 10001 JWTs antes de limpiar");
        assertEquals(0, countAfter, "Debe limpiar todo cuando supera el threshold");
    }

    @Test
    void testMultipleUniqueJwts_AllReturnTrue() {
        // Act & Assert
        assertTrue(jwtTrackerService.isJwtUnique("jwt-alpha"));
        assertTrue(jwtTrackerService.isJwtUnique("jwt-beta"));
        assertTrue(jwtTrackerService.isJwtUnique("jwt-gamma"));
        assertTrue(jwtTrackerService.isJwtUnique("jwt-delta"));

        assertEquals(4, jwtTrackerService.getUsedJwtsCount());
    }

    @Test
    void testThreadSafety_ConcurrentAccess() throws InterruptedException {
        // Arrange
        final int numThreads = 10;
        final int jwtsPerThread = 100;
        Thread[] threads = new Thread[numThreads];

        // Act - Crear múltiples threads que agregan JWTs concurrentemente
        for (int i = 0; i < numThreads; i++) {
            final int threadId = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < jwtsPerThread; j++) {
                    jwtTrackerService.isJwtUnique("jwt-thread" + threadId + "-" + j);
                }
            });
            threads[i].start();
        }

        // Esperar a que todos los threads terminen
        for (Thread thread : threads) {
            thread.join();
        }

        // Assert
        int expectedCount = numThreads * jwtsPerThread;
        assertEquals(expectedCount, jwtTrackerService.getUsedJwtsCount(),
                "Debe manejar correctamente acceso concurrente");
    }
}

