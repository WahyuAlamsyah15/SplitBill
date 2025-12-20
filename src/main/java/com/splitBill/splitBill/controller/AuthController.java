package com.splitBill.splitBill.controller;

import com.splitBill.splitBill.dto.request.EmailRequest;
import com.splitBill.splitBill.dto.request.LoginRequest;
import com.splitBill.splitBill.dto.request.RegistrationRequest;
import com.splitBill.splitBill.dto.request.PasswordResetRequest;
import com.splitBill.splitBill.dto.response.AuthResponse;
import com.splitBill.splitBill.handler.ApiResponse;
import com.splitBill.splitBill.handler.BadRequestException;
import com.splitBill.splitBill.service.JwtUtil;
import com.splitBill.splitBill.service.TokenBlacklistService;
import com.splitBill.splitBill.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
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
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private TokenBlacklistService tokenBlacklistService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<?>> registerUser(@Valid @RequestBody RegistrationRequest registrationRequest) {
        userService.initiateRegistration(registrationRequest);
        String message = "Registration initiated. Please check your email for the verification link.";
        return new ResponseEntity<>(ApiResponse.success(message), HttpStatus.OK);
    }

    @GetMapping("/verify-account")
    public ResponseEntity<ApiResponse<?>> verifyAccount(@RequestParam String token) {
        userService.confirmVerification(token);
        return new ResponseEntity<>(ApiResponse.success("Email verified successfully. You can now log in."), HttpStatus.OK);
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
        userService.initiatePasswordReset(request.getEmail());
        return new ResponseEntity<>(ApiResponse.success("If an account with that email exists, a password reset link has been sent."), HttpStatus.OK);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        userService.resetPasswordWithToken(request.getToken(), request.getNewPassword());
        return new ResponseEntity<>(ApiResponse.success("Password has been reset successfully."), HttpStatus.OK);
    }
}
