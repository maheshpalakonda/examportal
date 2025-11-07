package com.exam.portal.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
    }

    public void sendBulkEmails(String[] recipients, String subject, String body) {
        for (String recipient : recipients) {
            try {
                sendEmail(recipient, subject, body);
                Thread.sleep(100); // Small delay to avoid rate limiting
            } catch (Exception e) {
                // Log error but continue with other emails
                System.err.println("Failed to send email to " + recipient + ": " + e.getMessage());
            }
        }
    }
}
