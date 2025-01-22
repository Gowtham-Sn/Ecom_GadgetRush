package com.excelr.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.io.File;
import java.io.IOException;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    public void sendLoginEmail(String toEmail) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(toEmail);
        helper.setSubject("Login Successful to GadgetRush");
        helper.setText("Login successful to GadgetRush. If it's not you, please write us an email at gadgetrush.notify@gmail.com.");

        File logoFile = new File("https://drive.google.com/file/d/1XD5zRwGsUNw06kE41OqtL8LgJ4pD8TkQ/view?usp=drive_link");
        if (logoFile.exists()) {
            helper.addAttachment("Logo.png", logoFile);
        }

        mailSender.send(message);
    }

    public void sendRegistrationEmail(String toEmail) throws MessagingException, IOException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(toEmail);
        helper.setSubject("Registration Successful to GadgetRush");
        helper.setText("Welcome to GadgetRush! Thank you for registering with us. Enjoy shopping for the best gadgets and electronics at unbeatable prices!");
        File logoFile = new File("https://drive.google.com/file/d/1XD5zRwGsUNw06kE41OqtL8LgJ4pD8TkQ/view?usp=drive_link");
        if (logoFile.exists()) {
            helper.addAttachment("Logo.png", logoFile);
        }

        mailSender.send(message);
    }
}