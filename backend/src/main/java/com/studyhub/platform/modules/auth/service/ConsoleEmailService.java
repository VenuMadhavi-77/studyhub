package com.studyhub.platform.modules.auth.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class ConsoleEmailService implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(ConsoleEmailService.class);

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetToken) {
        log.info("==================================================================");
        log.info("[EMAIL SERVICE] Password Reset Email Request");
        log.info("To: {}", toEmail);
        log.info("Reset Token: {}", resetToken);
        log.info("Reset Link: http://localhost:5173/reset-password?token={}", resetToken);
        log.info("==================================================================");
    }

    @Override
    public void sendNotificationEmail(String toEmail, String subject, String content) {
        log.info("[EMAIL NOTIFICATION] To: {} | Subject: {} | Content: {}", toEmail, subject, content);
    }
}
