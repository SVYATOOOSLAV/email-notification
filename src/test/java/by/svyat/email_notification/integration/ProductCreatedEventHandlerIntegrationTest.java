package by.svyat.email_notification.integration;

import by.svyat.email_notification.handler.ProductCreatedEventHandler;
import by.svyat.email_notification.repository.ProcessedEventEntity;
import by.svyat.email_notification.repository.ProcessedEventRepository;
import by.svyat.kafkacommon.event.ProductCreatedEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@EmbeddedKafka(
        kraft = true
)
@SpringBootTest(
        properties = "spring.kafka.consumer.bootstrap-servers=${spring.embedded.kafka.brokers}" // Сервера Embedded Kafka
)
public class ProductCreatedEventHandlerIntegrationTest {

    @SpyBean
    private ProductCreatedEventHandler handler;

    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;

    @MockBean
    private ProcessedEventRepository processedEventRepository;

    @MockBean
    private RestTemplate restTemplate;

    @Captor
    private ArgumentCaptor<String> messageIdCaptor;

    @Captor
    private ArgumentCaptor<String> messageKeyCaptor;

    @Captor
    private ArgumentCaptor<ProductCreatedEvent> productCreatedEventCaptor;

    @Test
    void handleCreatedProductFromKafkaTest() throws ExecutionException, InterruptedException {
        ProductCreatedEvent event = new ProductCreatedEvent();
        event.setPrice(new BigDecimal(100));
        event.setProductId(UUID.randomUUID().toString());
        event.setQuantity(1);
        event.setTitle("TEST_PRODUCT");

        String messageId = UUID.randomUUID().toString();
        String messageKey = event.getProductId();

        when(processedEventRepository.findByMessageId(messageId)).thenReturn(new ProcessedEventEntity());
        when(processedEventRepository.save(any(ProcessedEventEntity.class))).thenReturn(null);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), isNull(), eq(String.class)))
                .thenReturn(new ResponseEntity<>(HttpStatusCode.valueOf(200)));

        ProducerRecord<String, Object> record = new ProducerRecord<>(
                "product-created-event-topic",
                messageKey,
                event
        );
        record.headers().add("messageId", messageId.getBytes());
        record.headers().add(KafkaHeaders.RECEIVED_KEY, messageKey.getBytes());

        kafkaTemplate.send(record).get();

        verify(handler, timeout(5000).times(1)).handleEvent(
                messageIdCaptor.capture(),
                messageKeyCaptor.capture(),
                productCreatedEventCaptor.capture()
        );

        assertEquals(messageId, messageIdCaptor.getValue());
        assertEquals(messageKey, messageKeyCaptor.getValue());
        assertEquals(event, productCreatedEventCaptor.getValue());
    }
}
