package com.splitBill.splitBill.service;

import com.mailjet.client.ClientOptions;
import com.mailjet.client.MailjetClient;
import com.mailjet.client.MailjetRequest;
import com.mailjet.client.MailjetResponse;
import com.mailjet.client.resource.Emailv31;
import com.splitBill.splitBill.model.OtpPurpose;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Value("${mailjet.api-key}")
    private String mailjetApiKey;

    @Value("${mailjet.secret-key}")
    private String mailjetSecretKey;

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

        // IMPORTANT: Replace with your verified sender email in Mailjet
        String fromEmailAddress = "wahyualamsyahjk06gmail.com";
        String fromEmailName = "SplitBill App";

        System.out.println("Using Mailjet API Key starting with: " + mailjetApiKey.substring(0, Math.min(mailjetApiKey.length(), 4)));

        ClientOptions options = ClientOptions.builder()
                .apiKey(mailjetApiKey)
                .apiSecretKey(mailjetSecretKey)
                .build();

        MailjetClient client = new MailjetClient(options);

        MailjetRequest request = new MailjetRequest(Emailv31.resource)
                .property(Emailv31.MESSAGES, new JSONArray()
                        .put(new JSONObject()
                                .put(Emailv31.Message.FROM, new JSONObject()
                                        .put("Email", fromEmailAddress)
                                        .put("Name", fromEmailName))
                                .put(Emailv31.Message.TO, new JSONArray()
                                        .put(new JSONObject()
                                                .put("Email", to)))
                                .put(Emailv31.Message.SUBJECT, subject)
                                .put(Emailv31.Message.TEXTPART, text)));

        try {
            MailjetResponse response = client.post(request);
            if (response.getStatus() == 200) {
                 System.out.println("OTP email sent successfully to " + to + " via Mailjet API for " + purpose.name());
            } else {
                System.err.println("Failed to send OTP email via Mailjet API. Status: " + response.getStatus() + ", Data: " + response.getData());
                 throw new RuntimeException("Failed to send OTP email via Mailjet API.");
            }
        } catch (Exception e) {
            System.err.println("Unexpected error sending OTP email via Mailjet API: " + e.getMessage());
            throw new RuntimeException("Unexpected error sending OTP email via Mailjet API", e);
        }
    }
}
