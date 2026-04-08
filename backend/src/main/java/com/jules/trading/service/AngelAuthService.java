package com.jules.trading.service;

import com.jules.trading.config.AngelOneConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AngelAuthService {

    @Autowired
    private AngelOneConfig config;

    private String jwtToken;

    public boolean login() {
        if (config.getApiKey() == null || config.getApiKey().isEmpty() || config.getTotpSecret() == null) {
            return false; // Keys not provided, stick to simulated fallback
        }

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
            headers.set("X-PrivateKey", config.getApiKey());

            String totp = TotpGenerator.getTotpCode(config.getTotpSecret());

            Map<String, String> body = new HashMap<>();
            body.put("clientcode", config.getClientId());
            body.put("password", config.getPin());
            body.put("totp", totp);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://apiconnect.angelbroking.com/rest/auth/angelbroking/user/v1/loginByPassword",
                    request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null) {
                    this.jwtToken = (String) data.get("jwtToken");
                    System.out.println("✅ Successfully Authenticated with Angel One SmartAPI!");
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("❌ Failed to authenticate with Angel One: " + e.getMessage());
        }
        return false;
    }

    public String getJwtToken() {
        if (jwtToken == null) login();
        return jwtToken;
    }
}
