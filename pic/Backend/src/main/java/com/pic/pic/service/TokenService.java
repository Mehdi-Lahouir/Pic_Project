package com.pic.pic.service;

import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class TokenService {

    private final String envToken;
    private final AtomicReference<String> override = new AtomicReference<>(null);

    public TokenService(@Value("${sportident.token:}") String envToken) {
        this.envToken = envToken == null ? "" : envToken.trim();
    }

    public String getBearerToken() {
        String t = override.get();
        if (t != null && !t.isBlank()) return normalize(t);
        return normalize(envToken);
    }

    public void setOverrideToken(String token) {
        override.set(token == null ? null : token.trim());
    }

    public boolean hasToken() {
        return !getBearerToken().isBlank();
    }

    private String normalize(String t) {
        if (t == null) return "";
        if (t.startsWith("Bearer ")) return t;
        return "Bearer " + t;
    }
}
