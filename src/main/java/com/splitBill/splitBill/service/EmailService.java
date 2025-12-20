package com.splitBill.splitBill.service;

import com.splitBill.splitBill.model.OtpPurpose;
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
                System.out.println("OTP email sent successfully to " + to + " via Elastic Email API for " + purpose.name());
            } else {
                System.err.println("Failed to send OTP email via Elastic Email API. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
                throw new RuntimeException("Failed to send OTP email via Elastic Email API.");
            }
        } catch (HttpStatusCodeException e) {
            System.err.println("Error sending OTP email via Elastic Email API. Status: " + e.getStatusCode() + ", Response: " + e.getResponseBodyAsString());
            throw new RuntimeException("Error sending OTP email via Elastic Email API", e);
        } catch (Exception e) {
            System.err.println("Unexpected error sending OTP email via Elastic Email API: " + e.getMessage());
            throw new RuntimeException("Unexpected error sending OTP email via Elastic Email API", e);
        }
    }
}
