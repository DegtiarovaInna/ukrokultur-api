package de.ukrokultur.ukrokultur_api.config;

import de.ukrokultur.ukrokultur_api.auth.Role;
import de.ukrokultur.ukrokultur_api.auth.User;
import de.ukrokultur.ukrokultur_api.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.StringUtils;

@Configuration
public class DataInitializer {

    @Value("${admin1.email:}")
    private String admin1Email;
    @Value("${admin1.password:}")
    private String admin1Password;

    @Value("${admin2.email:}")
    private String admin2Email;
    @Value("${admin2.password:}")
    private String admin2Password;

    @Bean
    ApplicationRunner initAdmins(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            createAdminIfConfigured(userRepository, encoder, admin1Email, admin1Password);
            createAdminIfConfigured(userRepository, encoder, admin2Email, admin2Password);
        };
    }

    private void createAdminIfConfigured(UserRepository repo, PasswordEncoder encoder, String email, String password) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) return;

        if (!repo.existsByEmail(email)) {
            repo.save(new User(
                    email.trim(),
                    encoder.encode(password),
                    Role.ADMIN
            ));
        }
    }
}
