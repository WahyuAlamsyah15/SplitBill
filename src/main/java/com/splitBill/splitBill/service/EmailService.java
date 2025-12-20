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

import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Service
public class EmailService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${mailjet.api-key}")
    private String mailjetApiKey;

    @Value("${mailjet.secret-key}")
    private String mailjetSecretKey;

    @Value("${mailjet.api-endpoint}")
    private String mailjetApiEndpoint;

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
        // Mailjet uses Basic Authentication
        String auth = mailjetApiKey + ":" + mailjetSecretKey;
        byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
        String authHeader = "Basic " + new String(encodedAuth);
        headers.set("Authorization", authHeader);

        // IMPORTANT: Replace with your verified sender email in Mailjet
        // You must have verified this email address in Mailjet
        String fromEmailAddress = "wahyualamsyahjk06@gmail.com"; 
        String fromEmailName = "SplitBill App"; // Optional: Your app's name

        MailjetEmailRequest mailjetRequest = new MailjetEmailRequest();
        mailjetRequest.Messages = new ArrayList<>();
        
        MailjetEmailRequest.Message message = new MailjetEmailRequest.Message();
        message.From = new MailjetEmailRequest.EmailInfo(fromEmailAddress, fromEmailName);
        message.To = Collections.singletonList(new MailjetEmailRequest.EmailInfo(to, null));
        message.Subject = subject;
        message.TextPart = text;
        
        mailjetRequest.Messages.add(message);

        HttpEntity<MailjetEmailRequest> entity = new HttpEntity<>(mailjetRequest, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(mailjetApiEndpoint, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("OTP email sent successfully to " + to + " via Mailjet API for " + purpose.name());
            } else {
                System.err.println("Failed to send OTP email via Mailjet API. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
                throw new RuntimeException("Failed to send OTP email via Mailjet API.");
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("Error sending OTP email via Mailjet API. Status: " + e.getStatusCode() + ", Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Error sending OTP email via Mailjet API", e);
        } catch (Exception e) {
            System.err.println("Unexpected error sending OTP email via Mailjet API: " + e.getMessage());
            throw new RuntimeException("Unexpected error sending OTP email via Mailjet API", e);
        }
    }

    // Inner classes to model Mailjet API request body
    private static class MailjetEmailRequest {
        public List<Message> Messages;

        public static class EmailInfo { // Moved here and made public
            public String Email;
            public String Name;

            public EmailInfo(String email, String name) {
                this.Email = email;
                this.Name = name;
            }
        }
        
        public static class Message {
            public EmailInfo From;
            public List<EmailInfo> To;
            public String Subject;
            public String TextPart; // For plain text email
        }
    }
}
