package com.splitBill.splitBill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Random;

@Service
public class OtpService {

    private static final String OTP_PREFIX = "otp:";
    private static final int OTP_LENGTH = 6;
    private static final Duration OTP_EXPIRATION = Duration.ofMinutes(5); // OTP valid for 5 minutes

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public String generateAndStoreOtp(String email) {
        String otp = generateOtp();
        redisTemplate.opsForValue().set(OTP_PREFIX + email, otp, OTP_EXPIRATION);
        return otp;
    }

    public boolean validateOtp(String email, String otp) {
        String storedOtp = (String) redisTemplate.opsForValue().get(OTP_PREFIX + email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            redisTemplate.delete(OTP_PREFIX + email); // OTP is valid, remove it
            return true;
        }
        return false;
    }

    private String generateOtp() {
        Random random = new Random();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }
}
