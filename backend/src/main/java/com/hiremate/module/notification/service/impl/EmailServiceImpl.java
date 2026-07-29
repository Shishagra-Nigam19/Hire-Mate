package com.hiremate.module.notification.service.impl;

import com.hiremate.module.notification.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from-email:no-reply@hiremate.com}")
    private String fromEmail;

    @Value("${app.mail.sender-name:HireMate Platform}")
    private String senderName;

    @Override
    @Async("taskExecutor")
    public void sendVerificationEmail(String toEmail, String recipientName, String verificationToken) {
        String subject = "Verify your HireMate Account Email";
        String content = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2>Welcome to HireMate, " + recipientName + "!</h2>"
                + "<p>Please confirm your email address to activate your account:</p>"
                + "<p><a href='http://localhost:3000/verify-email?token=" + verificationToken + "' style='background: #8b5cf6; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Verify Email</a></p>"
                + "<p>Or copy this token: <code>" + verificationToken + "</code></p>"
                + "<br/><p>Best regards,<br/>The HireMate Team</p></div>";

        sendHtmlEmail(toEmail, subject, content);
    }

    @Override
    @Async("taskExecutor")
    public void sendPasswordResetEmail(String toEmail, String recipientName, String resetToken) {
        String subject = "HireMate Password Reset Request";
        String content = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2>Password Reset Request</h2>"
                + "<p>Hello " + recipientName + ",</p>"
                + "<p>You recently requested to reset your password. Click the link below to reset it (valid for 15 minutes):</p>"
                + "<p><a href='http://localhost:3000/reset-password?token=" + resetToken + "' style='background: #ef4444; color: white; padding: 10px 20px; text-decoration: none; border-radius: 5px;'>Reset Password</a></p>"
                + "<p>Or copy this token: <code>" + resetToken + "</code></p>"
                + "<p>If you did not request a password reset, please ignore this email.</p></div>";

        sendHtmlEmail(toEmail, subject, content);
    }

    @Override
    @Async("taskExecutor")
    public void sendApplicationStatusNotification(String toEmail, String recipientName, String jobTitle, String status) {
        String subject = "Application Update: " + jobTitle;
        String content = "<div style='font-family: Arial, sans-serif; padding: 20px;'>"
                + "<h2>Application Status Update</h2>"
                + "<p>Hello " + recipientName + ",</p>"
                + "<p>Your application status for <strong>" + jobTitle + "</strong> has been updated to: <strong>" + status + "</strong>.</p>"
                + "<p>Log into your HireMate candidate dashboard for details.</p></div>";

        sendHtmlEmail(toEmail, subject, content);
    }

    private void sendHtmlEmail(String toEmail, String subject, String content) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, senderName);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            mailSender.send(message);
            log.info("Email successfully sent to: {} with subject: {}", toEmail, subject);
        } catch (Exception ex) {
            log.error("Failed to send email to: {} - Subject: {}", toEmail, subject, ex);
        }
    }
}
