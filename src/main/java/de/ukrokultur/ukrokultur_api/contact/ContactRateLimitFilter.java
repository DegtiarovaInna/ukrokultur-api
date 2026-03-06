package de.ukrokultur.ukrokultur_api.contact;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ContactRateLimitFilter extends OncePerRequestFilter {

    private final ContactRateLimitProperties props;
    private final Cache<String, AtomicInteger> counters;

    public ContactRateLimitFilter(ContactRateLimitProperties props) {
        this.props = props;
        this.counters = Caffeine.newBuilder()
                .expireAfterWrite(props.getWindow())
                .maximumSize(20_000)
                .build();
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!props.isEnabled()) {
            return true;
        }

        String path = request.getRequestURI();
        return !(path != null
                && path.startsWith("/contact")
                && "POST".equalsIgnoreCase(request.getMethod()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain
    ) throws ServletException, IOException {

        String key = resolveClientIp(request);

        AtomicInteger c = counters.get(key, k -> new AtomicInteger(0));
        if (c.incrementAndGet() > props.getMaxRequests()) {
            throw new ApiException(429, ErrorCode.TOO_MANY_REQUESTS, "Too many requests");
        }

        chain.doFilter(request, response);
    }

    private String resolveClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            String first = xff.split(",")[0].trim();
            if (!first.isBlank()) return first;
        }
        return request.getRemoteAddr();
    }
}