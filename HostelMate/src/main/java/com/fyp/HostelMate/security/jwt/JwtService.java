package com.fyp.HostelMate.security.jwt;

import com.fyp.HostelMate.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

// Handles all JWT token operations: generation, parsing, and validation.
// The secret is read from application.properties so we never hardcode it here.
@Service
public class JwtService {

    // Injected from application.properties - keeps secrets out of source code
    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    // Build the signing key from the secret string
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    // Generate a JWT token that contains the user's email and role as claims.
    // The frontend uses the role claim to decide which dashboard to show.
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("userId", user.getUserId().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Extract the email (subject) from a token
    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    // Extract the role claim from a token
    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    // Check if the token is still valid (not expired and signature matches)
    public boolean isTokenValid(String token) {
        try {
            parseClaims(token);  // will throw if expired or tampered
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Parse and return all claims from the token.
    // Throws an exception if the token is expired or the signature is wrong.
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
