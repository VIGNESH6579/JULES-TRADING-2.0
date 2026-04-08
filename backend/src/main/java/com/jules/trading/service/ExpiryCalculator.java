package com.jules.trading.service;

import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
public class ExpiryCalculator {

    public static class ExpiryInfo {
        public String dateString;
        public int daysToExpiry;
        public List<String> availableExpiries;
        
        public ExpiryInfo(String d, int dte, List<String> available) {
            this.dateString = d;
            this.daysToExpiry = dte;
            this.availableExpiries = available;
        }
    }

    public ExpiryInfo getExpiryFor(String symbol, String selectedExpiryStr) {
        LocalDate today = LocalDate.now();
        List<String> availableExpiries = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MMM");

        LocalDate seedDate = today;

        switch (symbol) {
            case "NIFTY":
                seedDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.THURSDAY));
                break;
            case "BANKNIFTY":
                seedDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.WEDNESDAY));
                break;
            case "SENSEX":
                seedDate = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.FRIDAY));
                break;
            default:
                seedDate = today.plusDays(15);
                break;
        }

        // Generate 4 upcoming expiries
        for (int i = 0; i < 4; i++) {
            if (symbol.equals("NIFTY") || symbol.equals("BANKNIFTY") || symbol.equals("SENSEX")) {
                availableExpiries.add(seedDate.plusWeeks(i).format(formatter));
            } else {
                availableExpiries.add(seedDate.plusMonths(i).format(formatter));
            }
        }

        // Target Date resolution
        LocalDate targetExpiryDate = seedDate;
        String finalDateStr = availableExpiries.get(0);

        if (selectedExpiryStr != null && !selectedExpiryStr.isEmpty() && availableExpiries.contains(selectedExpiryStr)) {
            finalDateStr = selectedExpiryStr;
            int index = availableExpiries.indexOf(selectedExpiryStr);
            if (symbol.equals("NIFTY") || symbol.equals("BANKNIFTY") || symbol.equals("SENSEX")) {
                targetExpiryDate = seedDate.plusWeeks(index);
            } else {
                targetExpiryDate = seedDate.plusMonths(index);
            }
        }

        int dte = (int) ChronoUnit.DAYS.between(today, targetExpiryDate);
        return new ExpiryInfo(finalDateStr, Math.max(0, dte), availableExpiries);
    }
}
