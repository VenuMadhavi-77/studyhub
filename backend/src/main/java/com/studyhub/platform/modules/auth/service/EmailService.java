package com.studyhub.platform.modules.auth.service;

public interface EmailService {
    void sendPasswordResetEmail(String toEmail, String resetToken);
    void sendNotificationEmail(String toEmail, String subject, String content);
}
