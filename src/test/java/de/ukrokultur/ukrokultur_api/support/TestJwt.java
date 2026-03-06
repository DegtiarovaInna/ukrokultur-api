package de.ukrokultur.ukrokultur_api.support;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;

public final class TestJwt {
    private TestJwt() {}

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor adminJwt() {
        return withRoles("ADMIN");
    }

    public static SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor withRoles(String... roles) {
        List<GrantedAuthority> auths = Arrays.stream(roles)
                .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r)
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return jwt()
                .jwt(j -> j.subject("test@local.test"))
                .authorities(auths);
    }
}