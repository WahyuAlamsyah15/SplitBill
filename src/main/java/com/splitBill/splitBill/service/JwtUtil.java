package com.splitBill.splitBill.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.splitBill.splitBill.handler.JwtExpiredException;
import com.splitBill.splitBill.handler.JwtInvalidException;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        // Cast to our custom User model to access the getEmail() method
        return createToken(claims, ((com.splitBill.splitBill.model.User) userDetails).getEmail());
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            throw new JwtExpiredException("JWT token is expired.");
        } catch (UnsupportedJwtException e) {
            throw new JwtInvalidException("JWT token is unsupported.");
        } catch (MalformedJwtException e) {
            throw new JwtInvalidException("Invalid JWT token.");
        } catch (SignatureException e) {
            throw new JwtInvalidException("Invalid JWT signature.");
        } catch (IllegalArgumentException e) {
            throw new JwtInvalidException("JWT claims string is empty.");
        }
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        final String emailFromToken = extractUsername(token); // This is now the email
        // Cast to our custom User model to access the getEmail() method for comparison
        return (emailFromToken.equals(((com.splitBill.splitBill.model.User) userDetails).getEmail()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}
