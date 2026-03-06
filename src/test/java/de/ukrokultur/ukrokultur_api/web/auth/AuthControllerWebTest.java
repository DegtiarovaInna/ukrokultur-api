package de.ukrokultur.ukrokultur_api.web.auth;

import tools.jackson.databind.ObjectMapper;
import de.ukrokultur.ukrokultur_api.auth.AuthController;
import de.ukrokultur.ukrokultur_api.common.dto.LoginRequestDto;
import de.ukrokultur.ukrokultur_api.config.security.JwtService;
import de.ukrokultur.ukrokultur_api.contact.ContactRateLimitProperties;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerWebTest {

    @Autowired MockMvc mvc;
    @Autowired ObjectMapper om;

    @MockitoBean AuthenticationManager authManager;
    @MockitoBean JwtService jwtService;

    @MockitoBean ContactRateLimitProperties contactRateLimitProperties;

    @Test
    void login_ok_setsCookie_andReturnsBearer() throws Exception {
        var req = new LoginRequestDto("admin@test.com", "pass");

        var principal = User.withUsername("admin@test.com")
                .password("x")
                .roles("ADMIN")
                .build();

        var auth = new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );

        when(authManager.authenticate(any())).thenReturn(auth);
        when(jwtService.generate(any())).thenReturn("jwt-token-123");

        mvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("jwt-token-123"))
                .andExpect(header().string("Set-Cookie", containsString("admin_session=")));
    }

    @Test
    void login_invalidCredentials_returns401ApiError() throws Exception {
        var req = new LoginRequestDto("admin@test.com", "wrong");

        when(authManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        mvc.perform(post("/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    @Test
    void logout_clearsCookie() throws Exception {
        mvc.perform(post("/auth/logout").with(csrf()))
                .andExpect(status().isOk())
                .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
    }
}