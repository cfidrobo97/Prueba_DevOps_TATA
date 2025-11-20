package com.devops.devops_service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {
    @Value("${security.jwt-secret}")
    private String secret;

    public String generateToken(String to, String from) {
        return Jwts.builder()
                .claim("to", to)
                .claim("from", from)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 60000))
                .signWith(SignatureAlgorithm.HS256, secret.getBytes())
                .compact();
    }
}
