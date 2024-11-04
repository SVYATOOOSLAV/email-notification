package by.svyat.email_notification.config;

import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableConfigurationProperties(value = KafkaConsumerConfig.class)
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaConsumerConfig kafkaConsumerConfig;

    public Map<String, Object> consumerFactory() {
        Map<String, Object> config = new HashMap<>();

        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConsumerConfig.getBootstrapServers());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConsumerConfig.getKeyDeserializer());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConsumerConfig.getValueDeserializer());
        config.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConsumerConfig.getGroupId());
        config.put(JsonDeserializer.TRUSTED_PACKAGES, kafkaConsumerConfig.getTrustedPackages());

        return config;
    }

    @Bean
    public ConsumerFactory<String, Object> kafkaConsumerFactory() {
        return new DefaultKafkaConsumerFactory<>(consumerFactory());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory(
            ConsumerFactory<String, Object> consumerFactory
    ) {
        return new ConcurrentKafkaListenerContainerFactory<>(){{
            setConsumerFactory(consumerFactory);
        }};
    }
}
