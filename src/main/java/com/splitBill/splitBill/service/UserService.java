package com.splitBill.splitBill.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.splitBill.splitBill.dto.request.RegistrationRequest;
import com.splitBill.splitBill.dto.temp.StagedRegistration;
import com.splitBill.splitBill.handler.BadRequestException;
import com.splitBill.splitBill.model.User;
import com.splitBill.splitBill.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.Optional;

@Service
public class UserService {

    private static final String REGISTRATION_PREFIX = "registration:";
    private static final Duration REGISTRATION_EXPIRATION = Duration.ofMinutes(15);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private VerificationTokenService verificationTokenService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Transactional
    public void initiateRegistration(RegistrationRequest registrationRequest) {
        if (userRepository.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException("Email already registered: " + registrationRequest.getEmail());
        }
        if (userRepository.findByUsername(registrationRequest.getUsername()).isPresent()) {
            throw new BadRequestException("Username already taken: " + registrationRequest.getUsername());
        }

        String hashedPassword = passwordEncoder.encode(registrationRequest.getPassword());
        StagedRegistration stagedUser = new StagedRegistration(
                registrationRequest.getUsername(),
                registrationRequest.getEmail(),
                hashedPassword
        );

        try {
            String stagedUserJson = objectMapper.writeValueAsString(stagedUser);
            String redisKey = REGISTRATION_PREFIX + registrationRequest.getEmail();
            redisTemplate.opsForValue().set(redisKey, stagedUserJson, REGISTRATION_EXPIRATION);

            String token = verificationTokenService.generateAndStoreToken(registrationRequest.getEmail());
            emailService.sendVerificationLinkEmail(registrationRequest.getEmail(), token);

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize staged registration data", e);
        }
    }

    @Transactional
    public void confirmVerification(String token) {
        String email = verificationTokenService.getEmailByTokenAndValidate(token);
        if (email == null) {
            throw new BadRequestException("Invalid or expired verification token.");
        }

        String redisKey = REGISTRATION_PREFIX + email;
        String stagedUserJson = redisTemplate.opsForValue().get(redisKey);
        
        if (stagedUserJson == null) {
            throw new BadRequestException("Registration session expired or invalid. Please register again.");
        }

        try {
            StagedRegistration stagedUser = objectMapper.readValue(stagedUserJson, StagedRegistration.class);

            User user = new User();
            user.setUsername(stagedUser.getUsername());
            user.setEmail(stagedUser.getEmail());
            user.setPassword(stagedUser.getHashedPassword()); // Already hashed
            user.setEnabled(true); // Enable user upon successful verification

            saveUser(user); // Use the original saveUser method to set tenant and save

            redisTemplate.delete(redisKey); // Clean up staged data

        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize staged registration data", e);
        }
    }
    
    public User saveUser(User user) {
        if (user.getTenantDbName() == null || user.getTenantDbName().isEmpty()) {
            String tenantName = "tenant-" + user.getUsername().replaceAll("\\s+", "").toLowerCase();
            user.setTenantDbName(tenantName);
        }
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findByUsername(String userName) {
        return userRepository.findByUsername(userName);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    public User updateUser(User user) {
        return userRepository.save(user);
    }

    public User updatePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        return userRepository.save(user);
    }
}
