package de.ukrokultur.ukrokultur_api.config;

import de.ukrokultur.ukrokultur_api.auth.Role;
import de.ukrokultur.ukrokultur_api.auth.User;
import de.ukrokultur.ukrokultur_api.auth.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final boolean enabled;


    private final String admin1Email;
    private final String admin1Password;
    private final String admin2Email;
    private final String admin2Password;

    public DataInitializer(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${security.bootstrap-admins.enabled:false}") boolean enabled,
            @Value("${security.bootstrap-admins.admin1.email:}") String admin1Email,
            @Value("${security.bootstrap-admins.admin1.password:}") String admin1Password,
            @Value("${security.bootstrap-admins.admin2.email:}") String admin2Email,
            @Value("${security.bootstrap-admins.admin2.password:}") String admin2Password
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.enabled = enabled;
        this.admin1Email = admin1Email;
        this.admin1Password = admin1Password;
        this.admin2Email = admin2Email;
        this.admin2Password = admin2Password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!enabled) return;

        List<AdminSeed> admins = new ArrayList<>();
        admins.add(new AdminSeed(admin1Email, admin1Password));
        admins.add(new AdminSeed(admin2Email, admin2Password));

        for (AdminSeed a : admins) {
            if (!StringUtils.hasText(a.email) || !StringUtils.hasText(a.password)) {
                continue;
            }
            if (userRepository.existsByEmail(a.email)) {
                continue;
            }

            User u = new User(a.email, passwordEncoder.encode(a.password), Role.ADMIN);
            userRepository.save(u);
        }
    }

    private record AdminSeed(String email, String password) {}
}