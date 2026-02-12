package de.ukrokultur.ukrokultur_api.config.security;

import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Configuration
public class SecurityConfig {

    @Value("${app.cors.allowed-origins:}")
    private String allowedOrigins;

    @Value("${app.cors.allowed-methods:GET,POST,PUT,DELETE,OPTIONS}")
    private String allowedMethods;

    @Value("${app.cors.allowed-headers:Authorization,Content-Type}")
    private String allowedHeaders;

    @Value("${app.cors.exposed-headers:Authorization}")
    private String exposedHeaders;

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            Environment env,
            JwtAuthenticationConverter jwtAuthenticationConverter

    ) throws Exception {

        boolean isDev = env.matchesProfiles("dev");

        http.csrf(csrf -> csrf.disable());
        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.cors(cors -> {});

        http.authorizeHttpRequests(auth -> {
            auth.requestMatchers("/news/**", "/contact/**", "/projects/**", "/home/**", "/about/**").permitAll();
            auth.requestMatchers("/auth/login").permitAll();

            if (isDev) {
                auth.requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).permitAll();
            } else {
                auth.requestMatchers(
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/v3/api-docs/**"
                ).denyAll();
            }

            auth.requestMatchers("/admin/**").hasRole("ADMIN");
            auth.anyRequest().denyAll();
        });

        http.oauth2ResourceServer(oauth -> oauth
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
                .authenticationEntryPoint((req, res, ex) ->
                        writeError(res, req, 401, "Unauthorized", ErrorCode.UNAUTHORIZED, "Unauthorized"))
                .accessDeniedHandler((req, res, ex) ->
                        writeError(res, req, 403, "Forbidden", ErrorCode.FORBIDDEN, "Forbidden"))
        );

        http.formLogin(f -> f.disable());
        http.httpBasic(b -> b.disable());

        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter gac = new JwtGrantedAuthoritiesConverter();
        gac.setAuthoritiesClaimName("roles");
        gac.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(gac);
        return converter;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();

        cfg.setAllowCredentials(false);

        List<String> origins = splitCsv(allowedOrigins);
        if (origins.isEmpty()) {
            origins = List.of("http://localhost:3000", "http://localhost:5173");
        }
        cfg.setAllowedOrigins(origins);

        cfg.setAllowedMethods(splitCsv(allowedMethods));
        cfg.setAllowedHeaders(splitCsv(allowedHeaders));
        cfg.setExposedHeaders(splitCsv(exposedHeaders));
        cfg.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", cfg);
        return source;
    }

    private static List<String> splitCsv(String csv) {
        if (csv == null || csv.isBlank()) return List.of();
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }

    private static void writeError(
            HttpServletResponse response,
            HttpServletRequest request,
            int status,
            String error,
            ErrorCode code,
            String message
    ) throws IOException {

        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);


        String safeMessage = jsonEscape(message);
        String safePath = jsonEscape(request.getRequestURI());

        String json = """
            {
              "timestamp": "%s",
              "status": %d,
              "error": "%s",
              "code": "%s",
              "message": "%s",
              "path": "%s",
              "fieldErrors": null
            }
            """.formatted(
                Instant.now(),
                status,
                error,
                code.name(),
                safeMessage,
                safePath
        );

        response.getWriter().write(json);
    }

    private static String jsonEscape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
