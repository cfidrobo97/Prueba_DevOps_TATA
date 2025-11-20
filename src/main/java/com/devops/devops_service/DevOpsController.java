package com.devops.devops_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DevOpsController {
    @Autowired
    JwtUtil jwtUtil;
    
    @Autowired
    JwtTrackerService jwtTrackerService;

    @PostMapping("/DevOps")
    public ResponseEntity<Map<String,String>> handle(
            @RequestHeader(value = "X-JWT-KWY", required = true) String incomingJwt,
            @RequestBody Map<String,Object> body) {

        // Validar que el JWT sea único (no reutilizado)
        if (!jwtTrackerService.isJwtUnique(incomingJwt)) {
            return ResponseEntity
                    .status(400)
                    .body(Map.of("error", "JWT reutilizado. Cada transacción requiere un JWT único."));
        }

        String message = (String) body.get("message");
        String to      = (String) body.get("to");
        String from    = (String) body.get("from");
        Integer ttl    = (Integer) body.get("timeToLifeSec");
        if (message==null||to==null||from==null||ttl==null) {
            return ResponseEntity.badRequest().body(Map.of("error","Payload inválido"));
        }

        // Generar un nuevo JWT para la respuesta
        String responseToken = jwtUtil.generateToken(to, from);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-JWT-KWY", responseToken);
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(Map.of("message", "Hello " + to + " your message will be sent"));
    }

    @RequestMapping(
            value = "/DevOps",
            method = {
                    RequestMethod.GET,
                    RequestMethod.PUT,
                    RequestMethod.DELETE,
                    RequestMethod.PATCH,
                    RequestMethod.OPTIONS
            }
    )
    public ResponseEntity<String> methodNotAllowed() {
        return ResponseEntity.status(405).body("ERROR");
    }
}
