package com.jules.trading.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AngelMarketDataService {

    @Autowired
    private AngelAuthService authService;

    // A fast memory cache in case Angel blocks due to rate limits (3 requests per sec max)
    private final Map<String, Double> fallbackCache = new HashMap<>();

    public AngelMarketDataService() {
        fallbackCache.put("NIFTY", 22405.0);
        fallbackCache.put("BANKNIFTY", 47805.0);
        fallbackCache.put("SENSEX", 73905.0);
        fallbackCache.put("CRUDEOIL", 6805.0);
        fallbackCache.put("NATGAS", 225.0);
    }

    public double getLiveSpotPrice(String symbol, String apiKey) {
        if (!authService.isConnected() || authService.getJwtToken() == null) {
            return fallbackCache.getOrDefault(symbol, Double.valueOf(22000.0));
        }

        String token = "26000"; // default nifty
        String exchange = "NSE";
        
        switch (symbol) {
            case "NIFTY": token = "26000"; break;
            case "BANKNIFTY": token = "26009"; break;
            case "SENSEX": token = "99926000"; exchange = "BSE"; break;
            default: return fallbackCache.getOrDefault(symbol, 0.0); // Keep MCX offline simulation for speed
        }

        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Accept", "application/json");
            headers.set("Authorization", "Bearer " + authService.getJwtToken());
            headers.set("X-PrivateKey", apiKey);
            headers.set("X-ClientLocalIP", "127.0.0.1");
            headers.set("X-ClientPublicIP", "127.0.0.1");
            headers.set("X-MACAddress", "11-22-33-44-55-66");
            headers.set("X-UserType", "USER");
            headers.set("X-SourceID", "WEB");

            Map<String, List<String>> exchangeTokens = new HashMap<>();
            exchangeTokens.put(exchange, Collections.singletonList(token));

            Map<String, Object> body = new HashMap<>();
            body.put("mode", "LTP");
            body.put("exchangeTokens", exchangeTokens);

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    "https://apiconnect.angelbroking.com/rest/secure/angelbroking/market/v1/quote",
                    request, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                Map<String, Object> data = (Map<String, Object>) response.getBody().get("data");
                if (data != null && data.containsKey("fetched")) {
                    List<Map<String, Object>> fetched = (List<Map<String, Object>>) data.get("fetched");
                    if (!fetched.isEmpty()) {
                        Object ltpObj = fetched.get(0).get("ltp");
                        if (ltpObj != null) {
                            double ltp = Double.parseDouble(ltpObj.toString());
                            fallbackCache.put(symbol, ltp); // Update cache
                            return ltp;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("API Rate limit hit or market closed. Returning cached price.");
        }
        
        return fallbackCache.get(symbol);
    }
}
