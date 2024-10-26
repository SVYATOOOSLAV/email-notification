package by.svyat.email_notification.handler;

import by.svyat.kafkacommon.event.ProductCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@KafkaListener(
        topics = {"product-created-event-topic"}
)
@Slf4j
public class ProductCreatedEventHandler {

    @KafkaHandler
    public void handleEvent(ProductCreatedEvent productCreatedEvent) {
        log.info("Received incoming product created event with productId: {}", productCreatedEvent.getProductId());
    }
}
