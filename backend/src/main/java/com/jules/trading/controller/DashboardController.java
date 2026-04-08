package com.jules.trading.controller;

import com.jules.trading.dto.DashboardResponse;
import com.jules.trading.dto.DashboardResponse.*;
import com.jules.trading.service.DataFetchService;
import com.jules.trading.service.OptionsAnalyzerService;
import com.jules.trading.service.AngelAuthService;
import com.jules.trading.service.AngelMarketDataService;
import com.jules.trading.service.ExpiryCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @Autowired
    private DataFetchService dataService;

    @Autowired
    private OptionsAnalyzerService analyzerService;

    @Autowired
    private AngelAuthService angelAuthService;
    
    @Autowired
    private AngelMarketDataService marketDataService;
    
    @Autowired
    private ExpiryCalculator expiryCalculator;

    private final Random random = new Random();

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam(defaultValue = "NIFTY") String symbol) {
        
        if (!angelAuthService.isConnected()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not connected to Angel One. Please login first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        // Live Market Call
        double spotPrice = marketDataService.getLiveSpotPrice(symbol, angelAuthService.getActiveApiKey());
        
        // Expiry Call
        ExpiryCalculator.ExpiryInfo expiry = expiryCalculator.getExpiryFor(symbol);

        List<OptionRow> chain = dataService.getSimulatedOptionChain(symbol, spotPrice);
        Analytics analytics = analyzerService.computeAnalytics(chain);
        Signal signal = analyzerService.generateSignal(analytics, spotPrice, chain);

        DashboardResponse response = new DashboardResponse();
        response.setSymbol(symbol);
        response.setSpotPrice(Math.round(spotPrice * 100.0) / 100.0);
        response.setExpiryDate(expiry.dateString);
        response.setDaysToExpiry(expiry.daysToExpiry);
        response.setChain(chain);
        response.setAnalytics(analytics);
        response.setSignal(signal);

        return ResponseEntity.ok(response);
    }
}
