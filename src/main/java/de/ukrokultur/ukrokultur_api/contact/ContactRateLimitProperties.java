package de.ukrokultur.ukrokultur_api.contact;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "contact.rate-limit")
public record ContactRateLimitProperties(
        boolean enabled,
        int capacity,
        Duration window
) {
}
