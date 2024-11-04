package by.svyat.email_notification.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "spring.kafka.consumer")
@Data
public class KafkaConsumerConfig {
    private String bootstrapServers;
    private String keyDeserializer;
    private String valueDeserializer;
    private String groupId;

    @Value("${spring.kafka.consumer.properties.spring.json.trusted.packages}")
    private String trustedPackages;
}
