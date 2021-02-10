package com.kafka.email.example.emailer.service;

import com.kafka.email.example.emailer.model.Notification;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private JavaMailSender emailSender;

    public EmailService (JavaMailSender emailSender){
        this.emailSender = emailSender;
    }

    public void sendEmail (Notification notification){
        String messageTemplate = "%s reported the following message:\n\n%s";
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@gmail.com");
        message.setTo("jthicks@gmail.com");
        message.setSubject(String.format("%s - %s", 
            notification.getType(), 
            notification.getService()
        ));
        message.setText(String.format(messageTemplate, 
            notification.getClazz(),
            notification.getMessage()
        ));

        emailSender.send(message);
    }

}
