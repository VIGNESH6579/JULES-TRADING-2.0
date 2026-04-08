package com.jules.trading.service;

import org.apache.commons.codec.binary.Base32;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;

public class TotpGenerator {

    public static String getTotpCode(String secretKey) {
        if (secretKey == null || secretKey.isEmpty()) return "000000";
        
        // Strip out any spaces the user accidentally typed
        secretKey = secretKey.trim().replaceAll(" ", "").toUpperCase();

        // If the user entered the 6-digit pin from Google Authenticator manually
        if (secretKey.length() == 6 && secretKey.matches("\\d+")) {
            return secretKey;
        }

        try {
            Base32 base32 = new Base32();
            byte[] bytes = base32.decode(secretKey);
            long timeWindow = System.currentTimeMillis() / 30000L;
            
            byte[] data = ByteBuffer.allocate(8).putLong(timeWindow).array();
            SecretKeySpec signKey = new SecretKeySpec(bytes, "HmacSHA1");
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(signKey);
            byte[] hash = mac.doFinal(data);
            
            int offset = hash[19] & 0xf;
            long truncatedHash = hash[offset] & 0x7f;
            for (int i = 1; i < 4; i++) {
                truncatedHash <<= 8;
                truncatedHash |= hash[offset + i] & 0xff;
            }
            truncatedHash %= 1000000;
            return String.format("%06d", truncatedHash);
        } catch (Exception e) {
            e.printStackTrace();
            return "000000";
        }
    }
}
