package de.ukrokultur.ukrokultur_api.support;

import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public abstract class AbstractJpaTcTest {

    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("ukrokultur_test")
                    .withUsername("test")
                    .withPassword("test");

    static {
        POSTGRES.start();
        Runtime.getRuntime().addShutdownHook(new Thread(POSTGRES::stop));
    }

    @DynamicPropertySource
    static void registerProps(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);

        r.add("spring.flyway.enabled", () -> "true");
        r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

        r.add("jwt.secret", () -> "test_test_test_test_test_test_test_32chars");
        r.add("jwt.issuer", () -> "ukrokultur-api-test");
        r.add("jwt.expiresMinutes", () -> "120");

        r.add("supabase.url", () -> "https://example.supabase.co");
        r.add("supabase.serviceRoleKey", () -> "test_service_key");
        r.add("supabase.bucket", () -> "ukrokultur");
        r.add("supabase.publicBaseUrl", () -> "");

        r.add("hcaptcha.enabled", () -> "false");
        r.add("contact.rate-limit.enabled", () -> "true");
        r.add("contact.rate-limit.capacity", () -> "5");
        r.add("contact.rate-limit.window", () -> "PT1M");

        r.add("resend.apiKey", () -> "test_resend_key");
        r.add("contact.mail.to", () -> "to@example.com");
        r.add("contact.mail.from", () -> "from@example.com");

        r.add("springdoc.api-docs.enabled", () -> "false");
        r.add("springdoc.swagger-ui.enabled", () -> "false");

        r.add("app.cors.allowed-origins", () -> "http://localhost:3000");
    }
}