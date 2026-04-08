package com.jules.trading.controller;

import com.jules.trading.dto.LoginRequest;
import com.jules.trading.service.AngelAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    @Autowired
    private AngelAuthService authService;

    @PostMapping("/connect-broker")
    public ResponseEntity<Map<String, String>> connectBroker(@RequestBody LoginRequest request) {
        Map<String, String> response = new HashMap<>();
        
        boolean success = authService.login(request);
        
        if (success) {
            response.put("message", "Connected successfully to Angel One!");
            return ResponseEntity.ok(response);
        } else {
            response.put("error", "Authentication failed. Check your keys or TOTP Secret.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
}
