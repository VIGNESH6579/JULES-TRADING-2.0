package com.jules.trading.service;

import com.jules.trading.dto.DashboardResponse.*;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OptionsAnalyzerService {

    public Analytics computeAnalytics(List<OptionRow> chain) {
        Analytics analytics = new Analytics();
        
        double totalCallOi = 0;
        double totalPutOi = 0;
        double maxPainPoints = Double.MAX_VALUE;
        double maxPainStrike = 0;

        for (OptionRow row : chain) {
            double ceOi = row.getCe() != null ? row.getCe().getOi() : 0;
            double peOi = row.getPe() != null ? row.getPe().getOi() : 0;
            
            totalCallOi += ceOi;
            totalPutOi += peOi;

            // Max Pain Calculation
            double currentStrikePain = 0;
            for (OptionRow innerRow : chain) {
                double strike = innerRow.getStrikePrice();
                double innerCeOi = innerRow.getCe() != null ? innerRow.getCe().getOi() : 0;
                double innerPeOi = innerRow.getPe() != null ? innerRow.getPe().getOi() : 0;

                // Call sellers lose if Expiry > Strike
                if (row.getStrikePrice() > strike) {
                    currentStrikePain += (row.getStrikePrice() - strike) * innerCeOi;
                }
                // Put sellers lose if Expiry < Strike
                if (row.getStrikePrice() < strike) {
                    currentStrikePain += (strike - row.getStrikePrice()) * innerPeOi;
                }
            }

            if (currentStrikePain < maxPainPoints) {
                maxPainPoints = currentStrikePain;
                maxPainStrike = row.getStrikePrice();
            }
        }

        analytics.setPcr(totalCallOi == 0 ? 0 : totalPutOi / totalCallOi);
        analytics.setMaxPain(maxPainStrike);
        analytics.setIvRank(Math.round(Math.random() * 40 + 30)); // Mocking IV Rank for now (30-70%)

        return analytics;
    }

    public Signal generateSignal(Analytics analytics, double spotPrice, List<OptionRow> chain) {
        Signal signal = new Signal();
        
        double pcr = analytics.getPcr();
        double maxPain = analytics.getMaxPain();

        if (pcr > 1.2 && spotPrice > maxPain) {
            signal.setAction("BUY");
            signal.setTarget(spotPrice + 150);
            signal.setStopLoss(spotPrice - 50);
            signal.setRationale("Strong Bullish momentum. PCR > 1.2 indicating Heavy Put Writing. Spot trading above Max Pain.");
        } else if (pcr < 0.8 && spotPrice < maxPain) {
            signal.setAction("SELL");
            signal.setTarget(spotPrice - 150);
            signal.setStopLoss(spotPrice + 50);
            signal.setRationale("Strong Bearish setup. PCR < 0.8 indicating Heavy Call Writing. Spot trading below Max Pain.");
        } else {
            signal.setAction("NEUTRAL");
            signal.setTarget(null);
            signal.setStopLoss(null);
            signal.setRationale("Conflicting Signals. PCR and MaxPain do not align perfectly. Wait for better entry.");
        }

        return signal;
    }
}
