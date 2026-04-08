package com.jules.trading.dto;

import lombok.Data;

@Data
public class LoginRequest {
    private String clientId;
    private String pin;
    private String apiKey;
    private String totpSecret;
}
