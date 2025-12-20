package com.splitBill.splitBill.service;

import com.splitBill.splitBill.model.TokenType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private static final Duration EMAIL_VERIFICATION_EXPIRATION = Duration.ofHours(24);
    private static final Duration PASSWORD_RESET_EXPIRATION = Duration.ofHours(1); // Shorter expiration for password reset

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public String generateAndStoreToken(String email, TokenType tokenType) {
        String token = UUID.randomUUID().toString();
        String redisKey = getRedisKey(token, tokenType);
        Duration expiration = tokenType == TokenType.PASSWORD_RESET ? PASSWORD_RESET_EXPIRATION : EMAIL_VERIFICATION_EXPIRATION;
        
        redisTemplate.opsForValue().set(redisKey, email, expiration);
        return token;
    }

    public String getEmailByTokenAndValidate(String token, TokenType tokenType) {
        String redisKey = getRedisKey(token, tokenType);
        String email = redisTemplate.opsForValue().get(redisKey);
        
        if (email != null) {
            redisTemplate.delete(redisKey); // Token is single-use, delete it after validation.
            return email;
        }
        return null;
    }

    private String getRedisKey(String token, TokenType tokenType) {
        return tokenType.name().toLowerCase() + ":" + token;
    }
}
