package com.splitBill.splitBill.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisStringTemplate;
    private final JwtUtil jwtUtil;
    private static final String BLACKLIST_PREFIX = "blacklist:";

    public void blacklistToken(String token) {
        try {
            Date expiration = jwtUtil.extractExpiration(token);
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisStringTemplate.opsForValue().set(BLACKLIST_PREFIX + token, "true", ttl, TimeUnit.MILLISECONDS);
            }
        } catch (ExpiredJwtException e) {
            // Token is already expired, no need to blacklist
        } catch (Exception e) {
            // Log other exceptions if necessary
            System.err.println("Error blacklisting token: " + e.getMessage());
        }
    }

    public boolean isTokenBlacklisted(String token) {
        return redisStringTemplate.hasKey(BLACKLIST_PREFIX + token);
    }
}
