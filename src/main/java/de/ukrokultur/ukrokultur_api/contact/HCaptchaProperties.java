package de.ukrokultur.ukrokultur_api.contact;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "hcaptcha")
public record HCaptchaProperties(
        boolean enabled,
        String secret
) {}
