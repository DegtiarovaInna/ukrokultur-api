package de.ukrokultur.ukrokultur_api.config;

import de.ukrokultur.ukrokultur_api.contact.ContactRateLimitProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({
        ContactRateLimitProperties.class
})
public class HttpClientConfig {

    @Bean
    public RestClient restClient(RestClient.Builder builder) {
        return builder.build();
    }
    @Bean
    public RestClient.Builder restClientBuilder() {
        return RestClient.builder();
    }
}
