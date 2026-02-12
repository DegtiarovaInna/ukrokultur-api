package de.ukrokultur.ukrokultur_api.config.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;

@Service
public class JwtService {

    private final JwtEncoder encoder;
    private final String issuer;
    private final long expiresMinutes;

    public JwtService(
            JwtEncoder encoder,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.expiresMinutes}") long expiresMinutes
    ) {
        this.encoder = encoder;
        this.issuer = issuer;
        this.expiresMinutes = expiresMinutes;
    }

    public String generate(UserDetails user) {
        Instant now = Instant.now();
        Instant exp = now.plusSeconds(expiresMinutes * 60);

        List<String> roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)   // например: ROLE_ADMIN
                .map(a -> a.startsWith("ROLE_") ? a.substring("ROLE_".length()) : a)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(exp)
                .subject(user.getUsername())
                .claim("roles", roles)
                .build();

        return encoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
