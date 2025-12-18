package com.splitBill.splitBill.service;

import com.splitBill.splitBill.model.OtpPurpose;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

@Service
public class EmailService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${resend.api-key}")
    private String resendApiKey;

    @Value("${resend.api-endpoint}")
    private String resendApiEndpoint;

    public void sendOtpEmail(String to, String otp, OtpPurpose purpose) {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(resendApiKey);

        // IMPORTANT: Replace with your verified sender email in Resend
        // You must have verified this email address or its domain in Resend
        String fromEmail = "alamsyahwahyu749@gmail.com"; 

        ResendEmailRequest request = new ResendEmailRequest(fromEmail, to, subject, text);
        HttpEntity<ResendEmailRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(resendApiEndpoint, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("OTP email sent successfully to " + to + " via Resend API for " + purpose.name());
            } else {
                System.err.println("Failed to send OTP email via Resend API. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
                throw new RuntimeException("Failed to send OTP email via Resend API.");
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("Error sending OTP email via Resend API. Status: " + e.getStatusCode() + ", Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Error sending OTP email via Resend API", e);
        } catch (Exception e) {
            System.err.println("Unexpected error sending OTP email via Resend API: " + e.getMessage());
            throw new RuntimeException("Unexpected error sending OTP email via Resend API", e);
        }
    }

    // Inner class to model Resend API request body
    private static class ResendEmailRequest {
        public String from;
        public String to;
        public String subject;
        public String text;

        public ResendEmailRequest(String from, String to, String subject, String text) {
            this.from = from;
            this.to = to;
            this.subject = subject;
            this.text = text;
        }
    }
}
