package com.splitBill.splitBill.controller;

import com.splitBill.splitBill.dto.request.EmailRequest;
import com.splitBill.splitBill.dto.request.LoginRequest;
import com.splitBill.splitBill.dto.request.RegistrationRequest;
import com.splitBill.splitBill.dto.request.PasswordResetRequest;
import com.splitBill.splitBill.dto.response.AuthResponse;
import com.splitBill.splitBill.handler.ApiResponse;
import com.splitBill.splitBill.handler.BadRequestException;
import com.splitBill.splitBill.handler.ResourceNotFoundException;
import com.splitBill.splitBill.model.OtpPurpose; // Import new enum
import com.splitBill.splitBill.model.User;
import com.splitBill.splitBill.service.EmailService;
import com.splitBill.splitBill.service.JwtUtil;
import com.splitBill.splitBill.service.OtpService;
import com.splitBill.splitBill.service.TokenBlacklistService;
import com.splitBill.splitBill.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import java.util.Optional;

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

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
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
        
        userService.saveUser(user);

        String otp = otpService.generateAndStoreOtp(user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp, OtpPurpose.REGISTRATION);

        String message = "Registration successful. Please verify your email with the OTP sent to " + user.getEmail();
        return new ResponseEntity<>(ApiResponse.success(message), HttpStatus.OK);
    }

    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<?>> verifyEmail(@RequestParam String email, @RequestParam String otp) {
        Optional<User> userOptional = userService.findByEmail(email);
        if (userOptional.isEmpty()) {
            throw new ResourceNotFoundException("User not found with email: " + email);
        }

        User user = userOptional.get();
        if (user.isEnabled()) {
            return new ResponseEntity<>(ApiResponse.success("Email already verified."), HttpStatus.OK);
        }

        if (otpService.validateOtp(email, otp)) {
            user.setEnabled(true);
            userService.updateUser(user);
            return new ResponseEntity<>(ApiResponse.success("Email verified successfully. You can now log in."), HttpStatus.OK);
        } else {
            throw new BadRequestException("Invalid or expired OTP.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
        );

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        if (!userDetails.isEnabled()) {
            throw new BadRequestException("Account not enabled. Please verify your email.");
        }

        String jwt = jwtUtil.generateToken(userDetails);
        AuthResponse authResponse = new AuthResponse(jwt);

        return new ResponseEntity<>(ApiResponse.success("Login successful.", authResponse), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<?>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
            return new ResponseEntity<>(ApiResponse.success("Logout successful."), HttpStatus.OK);
        }
        throw new BadRequestException("Authorization header is missing or malformed.");
    }

    @PostMapping("/request-password-reset")
    public ResponseEntity<ApiResponse<?>> requestPasswordReset(@Valid @RequestBody EmailRequest request) {
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));
        
        String otp = otpService.generateAndStoreOtp(user.getEmail());
        emailService.sendOtpEmail(user.getEmail(), otp, OtpPurpose.PASSWORD_RESET);

        return new ResponseEntity<>(ApiResponse.success("OTP sent to your email for password reset."), HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        User user = userService.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + request.getEmail()));

        if (otpService.validateOtp(request.getEmail(), request.getOtp())) {
            userService.updatePassword(user, request.getNewPassword());
            return new ResponseEntity<>(ApiResponse.success("Password has been reset successfully."), HttpStatus.OK);
        } else {
            throw new BadRequestException("Invalid or expired OTP.");
        }
    }
}
