package de.ukrokultur.ukrokultur_api.security;

import de.ukrokultur.ukrokultur_api.support.TestContainersConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static de.ukrokultur.ukrokultur_api.support.TestJwt.withRoles;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "JWT_SECRET=0123456789abcdef0123456789abcdef"
})
@AutoConfigureMockMvc
@Import(TestContainersConfig.class)
class SecurityIntegrationTest {

    @Autowired
    MockMvc mvc;

    @Test
    void publicEndpoints_areAccessibleWithoutAuth() throws Exception {
        mvc.perform(get("/news")).andExpect(status().isOk());
        mvc.perform(get("/projects")).andExpect(status().isOk());
        mvc.perform(get("/home")).andExpect(status().isOk());
        mvc.perform(get("/about")).andExpect(status().isOk());
    }

    @Test
    void adminEndpoints_withoutAuth_return401() throws Exception {
        mvc.perform(get("/admin/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void adminEndpoints_withNonAdminRole_return403() throws Exception {
        mvc.perform(get("/admin/me").with(withRoles("FOO")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminEndpoints_withAdminRole_return200() throws Exception {
        mvc.perform(get("/admin/me").with(withRoles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").exists());
    }
}