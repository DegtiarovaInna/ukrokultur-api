package de.ukrokultur.ukrokultur_api.config.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.util.StringUtils;

public class CookieOrHeaderBearerTokenResolver implements BearerTokenResolver {

    private final String cookieName;

    public CookieOrHeaderBearerTokenResolver(String cookieName) {
        this.cookieName = cookieName;
    }

    @Override
    public String resolve(HttpServletRequest request) {
        String path = request.getRequestURI();


        boolean shouldResolveToken =
                path.startsWith("/admin/") ||
                        path.startsWith("/auth/");

        if (!shouldResolveToken) {
            return null;
        }

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(auth) && auth.startsWith("Bearer ")) {
            String token = auth.substring("Bearer ".length()).trim();
            return StringUtils.hasText(token) ? token : null;
        }

        Cookie[] cookies = request.getCookies();
        if (cookies == null || cookies.length == 0) return null;

        for (Cookie c : cookies) {
            if (cookieName.equals(c.getName())) {
                String token = c.getValue();
                return StringUtils.hasText(token) ? token.trim() : null;
            }
        }

        return null;
    }
}