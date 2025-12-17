package com.sentinel.backend_for_frontend_service.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Slf4j
@Component
public class JwtUtils {

    @Value("${security.jwt.secret:X9q2N8ZCnO3Tj48p1Fk6B2V0x8Teq9gHBV0SX1e2p6U=}")
    private String jwtSecret;

    /**
     * Extract userId from JWT token
     */
    public String extractUserId(String authorizationHeader) {
        try {
            String token = authorizationHeader.replace("Bearer ", "");

            byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);
            SecretKey key = new SecretKeySpec(keyBytes, "HmacSHA256");

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            // Try different claim names
            String userId = claims.get("userId", String.class);
            if (userId == null) {
                userId = claims.get("sub", String.class);
            }
            if (userId == null) {
                userId = claims.get("user_id", String.class);
            }

            log.debug("Extracted userId from JWT: {}", userId);
            return userId;
        } catch (Exception e) {
            log.error("Failed to extract userId from JWT: {}", e.getMessage());
            return null;
        }
    }
}
