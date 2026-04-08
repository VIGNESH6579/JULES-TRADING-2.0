package com.jules.trading.dto;

import lombok.Data;
import java.util.List;

@Data
public class DashboardResponse {
    private String symbol;
    private double spotPrice;
    private String expiryDate;
    private int daysToExpiry;
    private List<String> availableExpiries;
    private Signal signal;
    private Analytics analytics;
    private List<OptionRow> chain;

    @Data
    public static class Signal {
        private String action; // BUY, SELL, NEUTRAL
        private Double target;
        private Double stopLoss;
        private String rationale;
    }

    @Data
    public static class Analytics {
        private double pcr;
        private double maxPain;
        private double ivRank;
    }

    @Data
    public static class OptionRow {
        private double strikePrice;
        private OptionDetails ce;
        private OptionDetails pe;
    }

    @Data
    public static class OptionDetails {
        private double ltp;
        private double oi;
        private double volume;
        private double iv;
        private String buildup;
        private Greeks greeks;
    }

    @Data
    public static class Greeks {
        private double delta;
        private double theta;
        private double gamma;
        private double vega;
    }
}
