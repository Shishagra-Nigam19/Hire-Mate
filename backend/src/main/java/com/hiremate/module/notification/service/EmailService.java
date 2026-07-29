package com.hiremate.module.notification.service;

public interface EmailService {

    void sendVerificationEmail(String toEmail, String recipientName, String verificationToken);

    void sendPasswordResetEmail(String toEmail, String recipientName, String resetToken);

    void sendApplicationStatusNotification(String toEmail, String recipientName, String jobTitle, String status);
}
