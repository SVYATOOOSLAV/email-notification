package by.svyat.email_notification.config;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class EmailNotificationConfig {

    @Bean
    public RestTemplate restTemplate(
            RestTemplateBuilder restTemplateBuilder
    ) {
        return restTemplateBuilder
                .rootUri("http://localhost:9090")
                .setConnectTimeout(Duration.ofMillis(3000))
                .setReadTimeout(Duration.ofMillis(3000))
                .build();

    }
}
