package com.kafka.email.example.emailer.service;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import com.kafka.email.example.emailer.model.LogType;
import com.kafka.email.example.emailer.model.Notification;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
public class EmailServiceTest {

    @Mock
    private JavaMailSender emailSenderMock;

    @InjectMocks
    private EmailService emailServiceMock;
    
    @Test
    public void sendEmailBuildsAndSendsEmail(){
        Notification notificationError = Notification.builder()
                                                .service("some service name")
                                                .clazz("some class name")
                                                .message("this is a test")
                                                .type(LogType.ERROR)
                                                .build();
        
        SimpleMailMessage expectedMailMessage = new SimpleMailMessage();
        expectedMailMessage.setTo("jthicks@gmail.com");
        expectedMailMessage.setFrom("noreply@gmail.com");
        expectedMailMessage.setSubject("ERROR - some service name");
        expectedMailMessage.setText("some class name reported the following message:\n\nthis is a test");

        doNothing().when(emailSenderMock).send(expectedMailMessage);

        emailServiceMock.sendEmail(notificationError);

        verify(emailSenderMock).send(expectedMailMessage);
        verifyNoMoreInteractions(emailSenderMock);
    }
}
