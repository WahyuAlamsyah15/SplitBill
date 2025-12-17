package com.splitBill.splitBill.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.splitBill.splitBill.dto.request.LoginRequest;
import com.splitBill.splitBill.dto.request.RegistrationRequest;
import com.splitBill.splitBill.dto.response.AuthResponse;
import com.splitBill.splitBill.handler.BadRequestException;
import com.splitBill.splitBill.handler.ResourceNotFoundException;
import com.splitBill.splitBill.model.User;
import com.splitBill.splitBill.service.EmailService;
import com.splitBill.splitBill.service.JwtUtil;
import com.splitBill.splitBill.service.OtpService;
import com.splitBill.splitBill.service.UserService;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @Autowired
    private EmailService emailService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        if (userService.existsByEmail(registrationRequest.getEmail())) {
            throw new BadRequestException("Email already registered: " + registrationRequest.getEmail());
        }
        if (userService.findByUsername(registrationRequest.getUsername()).isPresent()) {
            throw new BadRequestException("Username already taken: " + registrationRequest.getUsername());
        }

        User user = new User();
        user.setUsername(registrationRequest.getUsername());
        user.setEmail(registrationRequest.getEmail());
        user.setPassword(registrationRequest.getPassword());
        // User is saved with enabled = false by default

        userService.saveUser(user);

        String otp = otpService.generateAndStoreOtp(user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp);

        return new ResponseEntity<>(new AuthResponse("Registration successful. Please verify your email with the OTP sent to " + user.getEmail(), null), HttpStatus.OK);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<AuthResponse> verifyEmail(@RequestParam String email, @RequestParam String otp) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }

        User user = userOptional.get();
        if (user.isEnabled()) {
            return new ResponseEntity<>(new AuthResponse("Email already verified.", null), HttpStatus.OK);
        }

        if (otpService.validateOtp(email, otp)) {
            user.setEnabled(true);
            userService.updateUser(user);
            return new ResponseEntity<>(new AuthResponse("Email verified successfully. You can now log in.", null), HttpStatus.OK);
        } else {
            throw new BadRequestException("Invalid or expired OTP.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // Check if user is enabled (email verified)
        if (!userDetails.isEnabled()) {
            throw new BadRequestException("Account not enabled. Please verify your email.");
        }

        String jwt = jwtUtil.generateToken(userDetails);
        return new ResponseEntity<>(new AuthResponse("Login successful.", jwt), HttpStatus.OK);
    }
}