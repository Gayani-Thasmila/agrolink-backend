package com.agrolink.backend.service;

import com.agrolink.backend.model.User;
import com.agrolink.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.Locale;

@Service
public class AdminTokenService {

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    @Value("${app.admin.token-secret:change-this-admin-secret}")
    private String tokenSecret;

    @Value("${app.admin.token-expiry-hours:12}")
    private long tokenExpiryHours;

    @Autowired
    private UserRepository userRepository;

    public String generateToken(User user) {
        long expiresAt = Instant.now().plusSeconds(tokenExpiryHours * 3600).getEpochSecond();
        String payload = user.getId() + "|" + safe(user.getEmail()) + "|" + safe(user.getRole()) + "|" + expiresAt;
        String signature = sign(payload);
        String tokenValue = payload + "|" + signature;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenValue.getBytes(StandardCharsets.UTF_8));
    }

    public User requireAdmin(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            throw new IllegalArgumentException("Admin authorization token is required");
        }

        String token = authorizationHeader.substring(7).trim();
        if (token.isEmpty()) {
            throw new IllegalArgumentException("Admin authorization token is required");
        }

        String decodedToken;
        try {
            decodedToken = new String(Base64.getUrlDecoder().decode(token), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid admin token");
        }

        String[] parts = decodedToken.split("\\|");
        if (parts.length != 5) {
            throw new IllegalArgumentException("Invalid admin token");
        }

        String payload = String.join("|", parts[0], parts[1], parts[2], parts[3]);
        String expectedSignature = sign(payload);
        if (!MessageDigest.isEqual(expectedSignature.getBytes(StandardCharsets.UTF_8), parts[4].getBytes(StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("Invalid admin token");
        }

        long expiresAt;
        int userId;
        try {
            userId = Integer.parseInt(parts[0]);
            expiresAt = Long.parseLong(parts[3]);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid admin token");
        }

        if (Instant.now().getEpochSecond() > expiresAt) {
            throw new IllegalArgumentException("Admin token expired");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Admin user not found"));

        if (!"ADMIN".equalsIgnoreCase(safe(user.getRole()))) {
            throw new IllegalArgumentException("Admin access denied");
        }

        return user;
    }

    private String sign(String payload) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(tokenSecret.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM));
            byte[] signature = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(signature);
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to sign admin token", ex);
        }
    }

    private String safe(String value) {
        return value == null ? "" : value.trim().toUpperCase(Locale.ROOT).equals(value.trim()) ? value.trim() : value.trim();
    }
}
