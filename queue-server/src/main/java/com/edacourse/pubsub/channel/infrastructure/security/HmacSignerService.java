package com.edacourse.pubsub.channel.infrastructure.security;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

public class HmacSignerService implements HmacSigner {
    @Override
    public String sign(String payload, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec keySpec = new SecretKeySpec(
                secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(keySpec);
            byte[] hash = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return "sha256=" + HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Error firmando payload: " + e.getMessage(), e);
        }
    }


    @Override
    public boolean verify(String payload, String secret, String signature) {
        String expected = sign(payload, secret);
        return expected.equals(signature);
    }
            
}
