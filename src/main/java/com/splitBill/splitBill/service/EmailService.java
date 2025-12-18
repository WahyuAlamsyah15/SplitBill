package com.splitBill.splitBill.service;

import com.splitBill.splitBill.model.OtpPurpose; // Import new enum
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender javaMailSender;

    public void sendOtpEmail(String to, String otp, OtpPurpose purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);

        String subject;
        String text;

        switch (purpose) {
            case REGISTRATION:
                subject = "Verifikasi Email Anda - Kode OTP";
                text = "Halo,\n\nTerima kasih telah mendaftar. Kode OTP Anda untuk verifikasi email adalah: " + otp + "\n\nKode ini berlaku selama 5 menit.\n\nSalam,\nTim SplitBill";
                break;
            case PASSWORD_RESET:
                subject = "Permintaan Reset Kata Sandi - Kode OTP";
                text = "Halo,\n\nKami menerima permintaan untuk mereset kata sandi akun Anda. Kode OTP Anda adalah: " + otp + "\n\nKode ini berlaku selama 5 menit.\n\nJika Anda tidak meminta reset kata sandi, harap abaikan email ini.\n\nSalam,\nTim SplitBill";
                break;
            default:
                subject = "Kode OTP Anda";
                text = "Halo,\n\nKode OTP Anda adalah: " + otp + "\n\nKode ini berlaku selama 5 menit.\n\nSalam,\nTim SplitBill";
                break;
        }

        message.setSubject(subject);
        message.setText(text);
        
        try {
            javaMailSender.send(message);
            System.out.println("OTP email sent successfully to " + to + " for " + purpose.name());
        } catch (MailException e) {
            System.err.println("Error sending OTP email to " + to + " for " + purpose.name() + ": " + e.getMessage());
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}
