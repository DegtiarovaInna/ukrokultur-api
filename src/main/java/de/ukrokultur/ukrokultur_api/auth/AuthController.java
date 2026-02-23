package de.ukrokultur.ukrokultur_api.auth;



import de.ukrokultur.ukrokultur_api.common.dto.LoginResponseDto;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import de.ukrokultur.ukrokultur_api.common.dto.LoginRequestDto;
import de.ukrokultur.ukrokultur_api.config.security.JwtService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "Auth", description = "Admin authentication (JWT login).")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authManager;
    private final JwtService jwtService;

    public AuthController(AuthenticationManager authManager, JwtService jwtService) {
        this.authManager = authManager;
        this.jwtService = jwtService;
    }
    @Operation(summary = "Login (JWT)", description = "Returns a Bearer JWT token for admin endpoints.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class)))
    })
    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody @Valid LoginRequestDto req) {
        try {
            Authentication auth = authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.email(), req.password())
            );
            Object principal = auth.getPrincipal();
            String token = jwtService.generate((org.springframework.security.core.userdetails.UserDetails) auth.getPrincipal());
            return LoginResponseDto.bearer(token);
        } catch (AuthenticationException ex) {
            throw new ApiException(401, ErrorCode.UNAUTHORIZED, "Invalid credentials");
        }
    }
}
