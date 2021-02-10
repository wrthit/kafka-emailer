package com.kafka.email.example.emailer.listener;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.HashMap;
import java.util.Map;

import com.kafka.email.example.emailer.configuration.KafkaConsumerConfiguration;
import com.kafka.email.example.emailer.model.LogType;
import com.kafka.email.example.emailer.model.Notification;
import com.kafka.email.example.emailer.service.EmailService;

import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;

@SpringBootTest
@DirtiesContext
@EmbeddedKafka(topics = { "logging" }, partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092",
        "port=9092" })
@ContextConfiguration(classes = {KafkaConsumerConfiguration.class})
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ErrorListenerIT {
    
    @SpyBean
    private ErrorListener errorListenerMock;

    @Autowired
    private EmbeddedKafkaBroker kafkaEmbedded;

    @MockBean
    private EmailService emailServiceMock;

    Producer<String, Notification> producer;
    private static final String TOPIC = "logging";

    @BeforeAll
    public void setup(){
        Map<String, Object> configs = new HashMap<>(KafkaTestUtils.producerProps(kafkaEmbedded));
        producer = new DefaultKafkaProducerFactory<String, Notification>(configs, new StringSerializer(), new JsonSerializer<>()).createProducer();   
    }

    @AfterAll
    public void teardown(){
        if(producer != null){
            producer.close();
        }
    }

    @Test
    public void kafkaListenerConfiguredCorrectlyToFilterForErrors(){
        Notification notificationError = Notification.builder()
                                                .service("some service name")
                                                .clazz("some class name")
                                                .message("this is a test")
                                                .type(LogType.ERROR)
                                                .build();

        Notification notificationInfo = Notification.builder()
                                                .type(LogType.INFO)
                                                .build();

        Notification notificationDebug = Notification.builder()
                                                .type(LogType.DEBUG)
                                                .build();
                                                
        Notification notificationWarning = Notification.builder()
                                                .type(LogType.WARNING)
                                                .build();
                                          
        producer.send(new ProducerRecord<String, Notification>(TOPIC, notificationError));
        producer.send(new ProducerRecord<String, Notification>(TOPIC, notificationInfo));
        producer.send(new ProducerRecord<String, Notification>(TOPIC, notificationDebug));
        producer.send(new ProducerRecord<String, Notification>(TOPIC, notificationWarning));
        
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            fail("InterruptException thrown. " + e.getMessage());
        }

        doNothing().when(emailServiceMock).sendEmail(notificationError);

        verify(errorListenerMock).emailError(eq(notificationError));
        verify(emailServiceMock).sendEmail(notificationError);
        verifyNoMoreInteractions(errorListenerMock, emailServiceMock);
    }

}
