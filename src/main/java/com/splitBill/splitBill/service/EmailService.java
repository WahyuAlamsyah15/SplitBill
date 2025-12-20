package com.splitBill.splitBill.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpStatusCodeException;

@Service
public class EmailService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${elasticemail.api-key}")
    private String elasticEmailApiKey;

    @Value("${elasticemail.api-endpoint}")
    private String elasticEmailApiEndpoint;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    public void sendVerificationLinkEmail(String to, String token) {
        String subject = "Verifikasi Email Anda untuk SplitBill";
        String verificationLink = frontendBaseUrl + "/verify-email?token=" + token;
        String text = "Halo,\n\nTerima kasih telah mendaftar. Silakan klik tautan di bawah ini untuk memverifikasi alamat email Anda:\n" + verificationLink + "\n\nTautan ini berlaku selama 24 jam.\n\nSalam,\nTim SplitBill";

        // IMPORTANT: Replace with your verified sender email in Elastic Email
        String fromEmailAddress = "wahyualamsyahjk06@gmail.com";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("apikey", elasticEmailApiKey);
        map.add("subject", subject);
        map.add("from", fromEmailAddress);
        map.add("to", to);
        map.add("bodyText", text);
        map.add("isTransactional", "true");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(elasticEmailApiEndpoint, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Verification link email sent successfully to " + to + " via Elastic Email API");
            } else {
                System.err.println("Failed to send verification email via Elastic Email API. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
                throw new RuntimeException("Failed to send verification email via Elastic Email API.");
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("Error sending verification email via Elastic Email API. Status: " + e.getStatusCode() + ", Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Error sending verification email via Elastic Email API", e);
        } catch (Exception e) {
            System.err.println("Unexpected error sending verification email via Elastic Email API: " + e.getMessage());
            throw new RuntimeException("Unexpected error sending verification email via Elastic Email API", e);
        }
    }

    public void sendPasswordResetLinkEmail(String to, String token) {
        String subject = "Permintaan Reset Kata Sandi Akun SplitBill Anda";
        String resetLink = frontendBaseUrl + "/reset-password?token=" + token;
        String text = "Halo,\n\nKami menerima permintaan untuk mereset kata sandi akun Anda. Silakan klik tautan di bawah ini untuk mengatur ulang kata sandi Anda:\n" + resetLink + "\n\nTautan ini berlaku selama 1 jam.\n\nJika Anda tidak meminta reset kata sandi, harap abaikan email ini.\n\nSalam,\nTim SplitBill";

        String fromEmailAddress = "wahyualamsyahjk06@gmail.com";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("apikey", elasticEmailApiKey);
        map.add("subject", subject);
        map.add("from", fromEmailAddress);
        map.add("to", to);
        map.add("bodyText", text);
        map.add("isTransactional", "true");

        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(elasticEmailApiEndpoint, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Password reset email sent successfully to " + to + " via Elastic Email API");
            } else {
                System.err.println("Failed to send password reset email via Elastic Email API. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
                throw new RuntimeException("Failed to send password reset email via Elastic Email API.");
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("Error sending password reset email via Elastic Email API. Status: " + e.getStatusCode() + ", Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Error sending password reset email via Elastic Email API", e);
        } catch (Exception e) {
            System.err.println("Unexpected error sending password reset email via Elastic Email API: " + e.getMessage());
            throw new RuntimeException("Unexpected error sending password reset email via Elastic Email API", e);
        }
    }
}
