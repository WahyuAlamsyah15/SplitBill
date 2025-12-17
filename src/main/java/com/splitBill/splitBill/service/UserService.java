package com.splitBill.splitBill.service;
import com.splitBill.splitBill.model.User;
import com.splitBill.splitBill.repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public User saveUser(User user) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        // Generate a unique tenant name based on the username
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
}
