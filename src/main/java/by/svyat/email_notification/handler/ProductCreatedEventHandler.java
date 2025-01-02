package by.svyat.email_notification.handler;

import by.svyat.email_notification.exception.NonRetryableException;
import by.svyat.email_notification.exception.RetryableException;
import by.svyat.email_notification.repository.ProcessedEventEntity;
import by.svyat.email_notification.repository.ProcessedEventRepository;
import by.svyat.kafkacommon.event.ProductCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

@Component
@KafkaListener(
        topics = {"product-created-event-topic"}
)
@Slf4j
@RequiredArgsConstructor
public class ProductCreatedEventHandler {

    private final RestTemplate restTemplate;

    private final ProcessedEventRepository processedEventRepository;

    @KafkaHandler
    @Transactional
    public void handleEvent(
            @Header("messageId") String messageId,
            @Header(KafkaHeaders.RECEIVED_KEY) String messageKey,
            @Payload ProductCreatedEvent productCreatedEvent
    ) {
        if (productCreatedEvent.getTitle() == null) {
            throw new NonRetryableException("Title of product does not exist");
        }

        var entity = processedEventRepository.findByMessageId(messageId);
        if (entity != null) {
            log.info("Received duplicated message with messageId: {}", messageId);
            return;
        }

        log.info("Received incoming product created event with productId: {}", productCreatedEvent.getProductId());

        try {
            ResponseEntity<String> response =  restTemplate.exchange("/response/200", HttpMethod.GET, null, String.class);
            if (response.getStatusCode().value() == HttpStatus.OK.value()) {
                log.info("Received response: {}", response.getBody());
            }
        } catch (ResourceAccessException e) {
            log.error(e.getMessage());
            throw new RetryableException(e);
        } catch (HttpServerErrorException e) {
            log.error(e.getMessage());
            throw new NonRetryableException(e);
        } catch (Exception e){
            log.error(e.getMessage());
            throw new NonRetryableException(e);
        }

        try{
            processedEventRepository.save(new ProcessedEventEntity(messageId, productCreatedEvent.getProductId()));
        } catch (DataIntegrityViolationException e){
            log.error(e.getMessage());
            throw new NonRetryableException(e);
        }
    }
}
