package com.afsheen.aiassistant.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Minimal stand-in for the host app's real JWT util (same jjwt 0.11.x shape:
 * subject = email/identifier, "role" and "userId" custom claims). The
 * secret is env-configured, not hardcoded - see application.yml.
 */
@Component
public class JwtUtil {

    private final String secret;

    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.secret = secret;
    }

    public String generateToken(String subject, String role, String userId) {
        return Jwts.builder()
                .setSubject(subject)
                .claim("role", role)
                .claim("userId", userId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 86_400_000))
                .signWith(SignatureAlgorithm.HS256, secret)
                .compact();
    }

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    public String extractUserId(String token) {
        return getClaims(token).get("userId", String.class);
    }

    private Claims getClaims(String token) {
        return Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
    }
}
