package com.jules.trading.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class AngelOneConfig {
    @Value("${ANGEL_API_KEY:}")
    private String apiKey;

    @Value("${ANGEL_CLIENT_ID:}")
    private String clientId;

    @Value("${ANGEL_PIN:}")
    private String pin;

    @Value("${ANGEL_TOTP_SECRET:}")
    private String totpSecret;
}
