package com.jules.trading.service;

import com.jules.trading.dto.LoginRequest;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AngelAuthService {

    private String jwtToken;
    private String activeApiKey;
    private boolean isConnected = false;

    public boolean login(LoginRequest request) {
        if (request.getApiKey() == null || request.getTotpSecret() == null) {
            return false; 
        }

        this.activeApiKey = request.getApiKey();

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("X-UserType", "USER");
            headers.set("X-SourceID", "WEB");
            headers.set("X-ClientLocalIP", "127.0.0.1");
            headers.set("X-ClientPublicIP", "127.0.0.1");
            headers.set("X-MACAddress", "11-22-33-44-55-66");
            headers.set("X-PrivateKey", request.getApiKey());

            String totp = TotpGenerator.getTotpCode(request.getTotpSecret());

            Map<String, String> body = new HashMap<>();
            body.put("clientcode", request.getClientId());
            body.put("password", request.getPin());
            body.put("totp", totp);

            HttpEntity<Map<String, String>> httpRequest = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://apiconnect.angelbroking.com/rest/auth/angelbroking/user/v1/loginByPassword",
                    httpRequest, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    this.jwtToken = (String) data.get("jwtToken");
                    this.isConnected = true;
                    System.out.println("✅ Successfully Authenticated with Angel One SmartAPI!");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to authenticate with Angel One: " + e.getMessage());
        }
        return false;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public String getJwtToken() {
        return jwtToken;
    }

    public String getActiveApiKey() {
        return activeApiKey;
    }
}
