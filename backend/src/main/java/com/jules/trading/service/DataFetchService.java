package com.jules.trading.service;

import com.jules.trading.dto.DashboardResponse.*;
import com.jules.trading.engine.BlackScholesEngine;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service
public class DataFetchService {

    @Autowired
    private BlackScholesEngine bsEngine;

    private final Random random = new Random();

    // Mock Live Data to bypass aggressive NSE blockades while demonstrating full capability
    public List<OptionRow> getSimulatedOptionChain(String symbol, double currentSpot) {
        List<OptionRow> chain = new ArrayList<>();
        
        double step = 50;
        if (symbol.equals("BANKNIFTY") || symbol.equals("SENSEX")) step = 100;
        if (symbol.equals("CRUDEOIL")) step = 20; // smaller strikes for MCX
        if (symbol.equals("NATGAS")) step = 1;

        double baseStrike = Math.round(currentSpot / step) * step;
        
        // Generate strikes around spot
        for (int i = -10; i <= 10; i++) {
            double strike = baseStrike + (i * step);
            OptionRow row = new OptionRow();
            row.setStrikePrice(strike);

            // Calls
            OptionDetails ce = new OptionDetails();
            ce.setLtp(calculateMockLtp(currentSpot, strike, true));
            ce.setOi(random.nextInt(500000) + 50000);
            ce.setIv(0.12 + (random.nextDouble() * 0.05));
            ce.setBuildup(getMockBuildup(ce.getOi(), true, ce.getLtp()));
            ce.setGreeks(bsEngine.calculateGreeks(currentSpot, strike, 0.02, ce.getIv(), true));
            row.setCe(ce);

            // Puts
            OptionDetails pe = new OptionDetails();
            pe.setLtp(calculateMockLtp(currentSpot, strike, false));
            pe.setOi(random.nextInt(500000) + 50000);
            pe.setIv(0.12 + (random.nextDouble() * 0.05));
            pe.setBuildup(getMockBuildup(pe.getOi(), false, pe.getLtp()));
            pe.setGreeks(bsEngine.calculateGreeks(currentSpot, strike, 0.02, pe.getIv(), false));
            row.setPe(pe);

            chain.add(row);
        }
        
        return chain;
    }

    private double calculateMockLtp(double spot, double strike, boolean isCall) {
        double intrinsic = isCall ? Math.max(0, spot - strike) : Math.max(0, strike - spot);
        double timeValue = 50 + random.nextInt(100);
        return Math.round((intrinsic + timeValue) * 10.0) / 10.0;
    }

    private String getMockBuildup(double oi, boolean isCall, double ltp) {
        int rand = random.nextInt(4);
        if (rand == 0) return "Long Buildup";
        if (rand == 1) return "Short Buildup";
        if (rand == 2) return "Short Covering";
        return "Long Unwinding";
    }
}
