package com.example.flashcards.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class JwtService {
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder DECODER = Base64.getUrlDecoder();

    private static byte[] decodeBase64Url(String s) {
        int pad = s.length() % 4;
        if (pad > 0) s = s + "=".repeat(4 - pad);
        return DECODER.decode(s);
    }
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String createToken(String username, String role) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> claims = new LinkedHashMap<>();
            claims.put("sub", username);
            claims.put("role", role);
            claims.put("exp", Instant.now().plusSeconds(expirationSeconds).getEpochSecond());

            String encodedHeader = encodeJson(header);
            String encodedClaims = encodeJson(claims);
            String signingInput = encodedHeader + "." + encodedClaims;
            return signingInput + "." + sign(signingInput);
        } catch (Exception exception) {
            throw new IllegalStateException("Could not create token", exception);
        }
    }

    public Optional<String> validateAndGetUsername(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }

            String signingInput = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(signingInput), parts[2])) {
                return Optional.empty();
            }

            Map<String, Object> claims = objectMapper.readValue(decodeBase64Url(parts[1]), MAP_TYPE);
            long expiration = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= expiration) {
                return Optional.empty();
            }

            Object subject = claims.get("sub");
            return subject instanceof String username && !username.isBlank()
                    ? Optional.of(username)
                    : Optional.empty();
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    public Optional<String> validateAndGetRole(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                return Optional.empty();
            }
            Map<String, Object> claims = objectMapper.readValue(decodeBase64Url(parts[1]), MAP_TYPE);
            Object role = claims.get("role");
            return role instanceof String r && !r.isBlank() ? Optional.of(r) : Optional.of("USER");
        } catch (Exception exception) {
            return Optional.of("USER");
        }
    }

    private String encodeJson(Object value) throws Exception {
        return ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String value) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret, "HmacSHA256"));
        return ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String expected, String actual) {
        if (expected.length() != actual.length()) {
            return false;
        }
        int result = 0;
        for (int index = 0; index < expected.length(); index++) {
            result |= expected.charAt(index) ^ actual.charAt(index);
        }
        return result == 0;
    }
}
