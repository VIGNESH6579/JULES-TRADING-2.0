package com.jules.trading.controller;

import com.jules.trading.dto.DashboardResponse;
import com.jules.trading.dto.DashboardResponse.*;
import com.jules.trading.service.DataFetchService;
import com.jules.trading.service.OptionsAnalyzerService;
import com.jules.trading.service.AngelAuthService;
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

    private final Random random = new Random();
    
    // Simulate Spot drift
    private double currentNifty = 22400.0;
    private double currentBankNifty = 47800.0;
    private double currentSensex = 73900.0;
    private double currentCrudeOil = 6800.0;
    private double currentNatGas = 220.0;

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(@RequestParam(defaultValue = "NIFTY") String symbol) {
        
        if (!angelAuthService.isConnected()) {
            Map<String, String> err = new HashMap<>();
            err.put("error", "Not connected to Angel One. Please login first.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(err);
        }

        double spotPrice = updateAndGetSpot(symbol);
        
        List<OptionRow> chain = dataService.getSimulatedOptionChain(symbol, spotPrice);
        Analytics analytics = analyzerService.computeAnalytics(chain);
        Signal signal = analyzerService.generateSignal(analytics, spotPrice, chain);

        DashboardResponse response = new DashboardResponse();
        response.setSymbol(symbol);
        response.setSpotPrice(Math.round(spotPrice * 100.0) / 100.0);
        response.setChain(chain);
        response.setAnalytics(analytics);
        response.setSignal(signal);

        return ResponseEntity.ok(response);
    }

    private double updateAndGetSpot(String symbol) {
        double drift = (random.nextDouble() - 0.5) * 15; // Random walk -7.5 to +7.5 points
        switch (symbol.toUpperCase()) {
            case "NIFTY":
                currentNifty += drift;
                return currentNifty;
            case "BANKNIFTY":
                currentBankNifty += (drift * 2.5);
                return currentBankNifty;
            case "SENSEX":
                currentSensex += (drift * 3.5);
                return currentSensex;
            case "CRUDEOIL":
                currentCrudeOil += (drift * 0.5); // Smaller drift
                return currentCrudeOil;
            case "NATGAS":
                currentNatGas += (drift * 0.05); // Tiny drift
                return currentNatGas;
            default:
                return currentNifty;
        }
    }
}
