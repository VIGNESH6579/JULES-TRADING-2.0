package com.jules.trading.engine;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.springframework.stereotype.Component;
import com.jules.trading.dto.DashboardResponse.Greeks;

@Component
public class BlackScholesEngine {

    private static final NormalDistribution STANDARD_NORMAL = new NormalDistribution(0, 1);
    private static final double RISK_FREE_RATE = 0.07; // 7% approximate Indian Risk Free Rate

    /**
     * Calculates Greeks based on the Black-Scholes model.
     */
    public Greeks calculateGreeks(double spot, double strike, double timeToExpiryYears, double iv, boolean isCall) {
        Greeks greeks = new Greeks();
        
        if (timeToExpiryYears <= 0 || iv <= 0) {
            return greeks; // Return 0s if expiry is reached or IV is zero
        }

        double d1 = (Math.log(spot / strike) + (RISK_FREE_RATE + Math.pow(iv, 2) / 2) * timeToExpiryYears) 
                    / (iv * Math.sqrt(timeToExpiryYears));
        double d2 = d1 - iv * Math.sqrt(timeToExpiryYears);

        double nd1 = STANDARD_NORMAL.cumulativeProbability(d1);
        double nd2 = STANDARD_NORMAL.cumulativeProbability(d2);
        double nPrimeD1 = STANDARD_NORMAL.density(d1);

        if (isCall) {
            greeks.setDelta(nd1);
            greeks.setTheta(-(spot * nPrimeD1 * iv) / (2 * Math.sqrt(timeToExpiryYears)) 
                            - RISK_FREE_RATE * strike * Math.exp(-RISK_FREE_RATE * timeToExpiryYears) * nd2);
        } else {
            greeks.setDelta(nd1 - 1);
            greeks.setTheta(-(spot * nPrimeD1 * iv) / (2 * Math.sqrt(timeToExpiryYears)) 
                            + RISK_FREE_RATE * strike * Math.exp(-RISK_FREE_RATE * timeToExpiryYears) * STANDARD_NORMAL.cumulativeProbability(-d2));
        }

        // Gamma and Vega are same for both Call and Put
        greeks.setGamma(nPrimeD1 / (spot * iv * Math.sqrt(timeToExpiryYears)));
        greeks.setVega(spot * Math.sqrt(timeToExpiryYears) * nPrimeD1 / 100); // Divided by 100 for 1% change format

        // Convert Theta to daily theta
        greeks.setTheta(greeks.getTheta() / 365.0);

        return greeks;
    }
}
