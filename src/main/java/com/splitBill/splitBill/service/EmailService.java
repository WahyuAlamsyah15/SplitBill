package com.splitBill.splitBill.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${sendgrid.from-email}")
    private String fromEmailAddress;

    public void sendVerificationLinkEmail(String to, String token) {
        String subject = "Verifikasi Email Anda untuk SplitBill";
        String verificationLink = frontendBaseUrl + "/verify-account?token=" + token; // Changed to verify-account
        String text = "Halo,\n\nTerima kasih telah mendaftar. Silakan klik tautan di bawah ini untuk memverifikasi alamat email Anda:\n" + verificationLink + "\n\nTautan ini berlaku selama 24 jam.\n\nSalam,\nTim SplitBill";

        String fromEmailName = "SplitBill App"; // You can externalize this as well if needed

        Email from = new Email(fromEmailAddress, fromEmailName);
        Email toEmail = new Email(to);
        Content content = new Content("text/plain", text);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Verification link email sent successfully to " + to + " via SendGrid API");
            } else {
                System.err.println("Failed to send verification email via SendGrid API. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
                throw new RuntimeException("Failed to send verification email via SendGrid API.");
            }
        } catch (IOException e) {
            System.err.println("Error sending verification email via SendGrid API: " + e.getMessage());
            throw new RuntimeException("Error sending verification email via SendGrid API", e);
        }
    }

    public void sendPasswordResetLinkEmail(String to, String token) {
        String subject = "Permintaan Reset Kata Sandi Akun SplitBill Anda";
        String resetLink = frontendBaseUrl + "/reset-password?token=" + token;
        String text = "Halo,\n\nKami menerima permintaan untuk mereset kata sandi akun Anda. Silakan klik tautan di bawah ini untuk mengatur ulang kata sandi Anda:\n" + resetLink + "\n\nTautan ini berlaku selama 1 jam.\n\nJika Anda tidak meminta reset kata sandi, harap abaikan email ini.\n\nSalam,\nTim SplitBill";

        String fromEmailName = "SplitBill App"; // You can externalize this as well if needed

        Email from = new Email(fromEmailAddress, fromEmailName);
        Email toEmail = new Email(to);
        Content content = new Content("text/plain", text);
        Mail mail = new Mail(from, subject, toEmail, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);

            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                System.out.println("Password reset email sent successfully to " + to + " via SendGrid API");
            } else {
                System.err.println("Failed to send password reset email via SendGrid API. Status: " + response.getStatusCode() + ", Body: " + response.getBody());
                throw new RuntimeException("Failed to send password reset email via SendGrid API.");
            }
        } catch (IOException e) {
            System.err.println("Error sending password reset email via SendGrid API: " + e.getMessage());
            throw new RuntimeException("Error sending password reset email via SendGrid API", e);
        }
    }
}
