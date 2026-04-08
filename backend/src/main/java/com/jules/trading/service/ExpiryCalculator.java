package com.jules.trading.service;

import org.springframework.stereotype.Service;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

@Service
public class ExpiryCalculator {

    public static class ExpiryInfo {
        public String dateString;
        public int daysToExpiry;
        
        public ExpiryInfo(String d, int dte) {
            this.dateString = d;
            this.daysToExpiry = dte;
        }
    }

    public ExpiryInfo getExpiryFor(String symbol) {
        LocalDate today = LocalDate.now();
        LocalDate expiryDate = today;

        switch (symbol) {
            case "NIFTY":
                // Nifty weekly expiry is Thursday
                expiryDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY));
                break;
            case "BANKNIFTY":
                // BankNifty weekly expiry is Wednesday
                expiryDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
                break;
            case "SENSEX":
                // Sensex weekly expiry is Friday
                expiryDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
                break;
            default:
                // Commodities roughly expire around the mid/late month, we approximate 15 days for UI 
                expiryDate = today.plusDays(15);
                break;
        }

        int dte = (int) ChronoUnit.DAYS.between(today, expiryDate);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM");
        
        return new ExpiryInfo(expiryDate.format(formatter), Math.max(0, dte));
    }
}
