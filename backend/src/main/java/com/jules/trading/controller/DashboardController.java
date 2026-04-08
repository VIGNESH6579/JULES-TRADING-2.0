package com.jules.trading.controller;

import com.jules.trading.dto.DashboardResponse;
import com.jules.trading.dto.DashboardResponse.*;
import com.jules.trading.service.DataFetchService;
import com.jules.trading.service.OptionsAnalyzerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/api")
public class DashboardController {

    @Autowired
    private DataFetchService dataService;

    @Autowired
    private OptionsAnalyzerService analyzerService;

    private final Random random = new Random();
    
    // Simulate Spot drift
    private double currentNifty = 22400.0;
    private double currentBankNifty = 47800.0;
    private double currentSensex = 73900.0;

    @GetMapping("/dashboard")
    public DashboardResponse getDashboard(@RequestParam(defaultValue = "NIFTY") String symbol) {
        
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

        return response;
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
            default:
                return currentNifty;
        }
    }
}
