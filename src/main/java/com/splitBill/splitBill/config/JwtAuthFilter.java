package com.splitBill.splitBill.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.splitBill.splitBill.service.JwtUtil;
import com.splitBill.splitBill.service.UserDetailsServiceImpl;
import com.splitBill.splitBill.service.TokenBlacklistService;
import com.splitBill.splitBill.handler.JwtInvalidException;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private TokenBlacklistService tokenBlacklistService; // Inject TokenBlacklistService

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);

            // NEW: Check if token is blacklisted
            if (tokenBlacklistService.isTokenBlacklisted(token)) {
                logger.warn("Attempt to use blacklisted token: {}", token);
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401
                response.getWriter().write("Token is blacklisted.");
                return; // Stop the filter chain
            }

            try {
                username = jwtUtil.extractUsername(token);
                logger.debug("Successfully extracted username: {} from token.", username);
            } catch (Exception e) {
                logger.error("Error extracting username from token: {}", e.getMessage());
                // Set response status to UNAUTHORIZED for invalid/malformed tokens
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token.");
                return; // Stop the filter chain
            }
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtil.validateToken(token, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
                logger.debug("Successfully authenticated user: {}", username);
            } else {
                logger.debug("Token validation failed for user: {}", username);
                // Also explicitly reject if validation fails
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Invalid or expired token.");
                return; // Stop the filter chain
            }
        } else if (username == null && authHeader != null && authHeader.startsWith("Bearer ")) {
            // This path would be hit if extractUsername fails, but we now handle that explicitly above.
            // Or if SecurityContext already contains authentication - which means token was valid
            // but we won't try to re-authenticate.
             logger.debug("Authentication not performed, possibly due to prior context or missing token in this path.");
        } else if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            logger.debug("No Bearer token found in Authorization header.");
        }
        filterChain.doFilter(request, response);
    }
}
