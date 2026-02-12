package de.ukrokultur.ukrokultur_api.config;

import de.ukrokultur.ukrokultur_api.contact.ResendProperties;
import de.ukrokultur.ukrokultur_api.media.SupabaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import de.ukrokultur.ukrokultur_api.contact.ContactMailProperties;
import de.ukrokultur.ukrokultur_api.contact.HCaptchaProperties;
import org.springframework.web.client.RestClient;
import org.springframework.cache.annotation.EnableCaching;
@Configuration
@EnableCaching
@EnableConfigurationProperties({
        SupabaseProperties.class,
        HCaptchaProperties.class,
        ContactMailProperties.class,
        ResendProperties.class
})
public class AppConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


}
