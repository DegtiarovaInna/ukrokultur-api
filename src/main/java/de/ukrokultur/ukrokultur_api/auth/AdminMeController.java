package de.ukrokultur.ukrokultur_api.auth;

import de.ukrokultur.ukrokultur_api.config.OpenApiConfig;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Admin", description = "Admin session utilities.")
@RestController
public class AdminMeController {

    @SecurityRequirement(name = OpenApiConfig.BEARER_SCHEME_NAME)
    @Operation(summary = "Who am I (admin)", description = "Returns current authenticated admin info (server-guard session check).")
    @GetMapping("/admin/me")
    public AdminMeDto me(Authentication auth) {
        String email = auth.getName();
        List<String> roles = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return new AdminMeDto(email, roles);
    }

    public record AdminMeDto(String email, List<String> roles) {}
}