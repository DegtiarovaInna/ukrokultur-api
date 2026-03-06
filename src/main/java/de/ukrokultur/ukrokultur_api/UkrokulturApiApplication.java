package de.ukrokultur.ukrokultur_api;

import de.ukrokultur.ukrokultur_api.contact.ContactRateLimitProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(ContactRateLimitProperties.class)
public class UkrokulturApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(UkrokulturApiApplication.class, args);
	}

}
