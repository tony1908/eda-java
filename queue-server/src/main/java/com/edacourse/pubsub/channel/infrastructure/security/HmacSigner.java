package com.edacourse.pubsub.channel.infrastructure.security;

public interface HmacSigner {
    String sign(String payload, String secret);
    boolean verify(String payload, String secret, String signature);
}
