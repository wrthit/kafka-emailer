package com.kafka.email.example.emailer.listener;

import com.kafka.email.example.emailer.model.Notification;
import com.kafka.email.example.emailer.service.EmailService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class ErrorListener {
    
    private EmailService emailService;

    public ErrorListener(EmailService emailService){
        this.emailService = emailService;
    }

    @KafkaListener(topics = "${topic}", groupId = "${groupId}", containerFactory = "kafkaListenerContainerFactory")
    public void emailError (@Payload Notification notification){
        emailService.sendEmail(notification);
    }
}
