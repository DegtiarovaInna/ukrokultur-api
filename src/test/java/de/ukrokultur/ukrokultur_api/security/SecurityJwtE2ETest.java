package de.ukrokultur.ukrokultur_api.security;

import de.ukrokultur.ukrokultur_api.support.TestContainersConfig;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
class SecurityJwtE2ETest {

    @Autowired MockMvc mvc;
    @Autowired JwtEncoder jwtEncoder;

    @Value("${jwt.issuer}")
    String issuer;

    @Value("${security.auth.cookie.name:admin_session}")
    String cookieName;

    private String tokenWithRoles(String subject, List<String> roles) {
        Instant now = Instant.now();
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(3600))
                .subject(subject)
                .claim("roles", roles)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Test
    void adminMe_acceptsBearerHeaderToken() throws Exception {
        String token = tokenWithRoles("admin@test.local", List.of("ADMIN"));

        mvc.perform(get("/admin/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.local"));
    }

    @Test
    void adminMe_acceptsCookieToken() throws Exception {
        String token = tokenWithRoles("admin@test.local", List.of("ADMIN"));

        mvc.perform(get("/admin/me").cookie(new Cookie(cookieName, token)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@test.local"));
    }

    @Test
    void adminMe_nonAdminRole_returns403() throws Exception {
        String token = tokenWithRoles("someone@test.local", List.of("FOO"));

        mvc.perform(get("/admin/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}