package com.splitBill.splitBill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Verifikasi Email Anda - Kode OTP");
        message.setText("Halo,\n\nTerima kasih telah mendaftar. Kode OTP Anda untuk verifikasi email adalah: " + otp + "\n\nKode ini berlaku selama 5 menit.\n\nSalam,\nTim SplitBill");
        try {
            javaMailSender.send(message);
            System.out.println("OTP email sent successfully to " + to);
        } catch (MailException e) {
            System.err.println("Error sending OTP email to " + to + ": " + e.getMessage());
            // Optionally rethrow as a custom exception
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
