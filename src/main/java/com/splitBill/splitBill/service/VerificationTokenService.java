package com.splitBill.splitBill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;

@Service
public class VerificationTokenService {

    private static final String VERIFICATION_PREFIX = "verify_token:";
    private static final Duration TOKEN_EXPIRATION = Duration.ofHours(24); // Token valid for 24 hours

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    /**
     * Generates a unique verification token and stores it in Redis.
     * The token is the key, and the user's email is the value.
     * @param email The user's email to associate with the token.
     * @return The generated unique token.
     */
    public String generateAndStoreToken(String email) {
        String token = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(VERIFICATION_PREFIX + token, email, TOKEN_EXPIRATION);
        return token;
    }

    /**
     * Validates a token. If valid, it returns the associated email and deletes the token.
     * @param token The token to validate.
     * @return The user's email if the token is valid, otherwise null.
     */
    public String getEmailByTokenAndValidate(String token) {
        String redisKey = VERIFICATION_PREFIX + token;
        String email = redisTemplate.opsForValue().get(redisKey);
        
        if (email != null) {
            redisTemplate.delete(redisKey); // Token is single-use, delete it after validation.
            return email;
        }
        return null;
    }
}
