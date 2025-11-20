package com.devops.devops_service;

import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Servicio para rastrear JWTs únicos por transacción.
 * Previene la reutilización de JWTs (replay attacks).
 */
@Service
public class JwtTrackerService {
    
    // Set thread-safe para almacenar JWTs ya utilizados
    private final Set<String> usedJwts = ConcurrentHashMap.newKeySet();
    
    /**
     * Valida que el JWT no haya sido usado previamente.
     * @param jwt El JWT a validar
     * @return true si el JWT es único, false si ya fue usado
     */
    public boolean isJwtUnique(String jwt) {
        if (jwt == null || jwt.trim().isEmpty()) {
            return false;
        }
        
        // Intenta agregar el JWT al set. 
        // Retorna true si se agregó (es nuevo), false si ya existía
        return usedJwts.add(jwt);
    }
    
    /**
     * Limpia JWTs antiguos para evitar crecimiento infinito de memoria.
     * En producción, esto debería usar una cache con TTL o base de datos.
     */
    public void clearOldJwts() {
        // Por simplicidad, limpia todo si hay más de 10000 JWTs
        if (usedJwts.size() > 10000) {
            usedJwts.clear();
        }
    }
    
    /**
     * Obtiene el número de JWTs únicos procesados.
     */ 
    public int getUsedJwtsCount() {
        return usedJwts.size();
    }
}

