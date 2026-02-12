package de.ukrokultur.ukrokultur_api.contact;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "contact.mail")
public record ContactMailProperties(
        String to,
        String from
) {}
