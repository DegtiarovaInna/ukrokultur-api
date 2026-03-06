package de.ukrokultur.ukrokultur_api.common.error;

import de.ukrokultur.ukrokultur_api.common.exception.NotFoundException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = GlobalExceptionHandlerWebTest.TestController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import({
        GlobalExceptionHandler.class,
        GlobalExceptionHandlerWebTest.TestController.class
})
class GlobalExceptionHandlerWebTest {

    @Autowired
    MockMvc mvc;

    @RestController
    @RequestMapping("/test")
    static class TestController {

        @PostMapping(path = "/validation", consumes = MediaType.APPLICATION_JSON_VALUE)
        String validation(@Valid @RequestBody TestDto dto) {
            return "ok";
        }

        @GetMapping("/not-found")
        String notFound() {
            throw new NotFoundException("Not found");
        }

        @GetMapping("/auth")
        String auth() {
            throw new BadCredentialsException("bad credentials");
        }

        @GetMapping("/denied")
        String denied() {
            throw new AccessDeniedException("no access");
        }

        @PostMapping(path = "/bad-json", consumes = MediaType.APPLICATION_JSON_VALUE)
        String badJson(@RequestBody Map<String, Object> body) {
            return "ok";
        }

        @PostMapping(path = "/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
        String multipartRequired(@RequestPart("file") MultipartFile file) {
            return "ok";
        }

        @PostMapping(path = "/unsupported", consumes = MediaType.APPLICATION_XML_VALUE)
        String unsupported(@RequestBody String xml) {
            return "ok";
        }

        @GetMapping("/generic")
        String generic() {
            throw new RuntimeException("boom");
        }
    }

    static class TestDto {

        @NotBlank(message = "name must not be blank")
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }



    @Test
    void validation_returns400_withFieldErrors() throws Exception {
        mvc.perform(post("/test/validation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/test/validation"))
                .andExpect(jsonPath("$.fieldErrors.name").value("name must not be blank"));
    }

    @Test
    void apiException_returns404_andCodeNotFound() throws Exception {
        mvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.code").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Not found"))
                .andExpect(jsonPath("$.path").value("/test/not-found"))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void authenticationException_returns401() throws Exception {
        mvc.perform(get("/test/auth"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"))
                .andExpect(jsonPath("$.message").value("Unauthorized"))
                .andExpect(jsonPath("$.path").value("/test/auth"))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void accessDenied_returns403() throws Exception {
        mvc.perform(get("/test/denied"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.code").value("FORBIDDEN"))
                .andExpect(jsonPath("$.message").value("Forbidden"))
                .andExpect(jsonPath("$.path").value("/test/denied"))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void malformedJson_returns400() throws Exception {
        mvc.perform(post("/test/bad-json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{bad-json"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/test/bad-json"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void missingMultipartPart_returns400() throws Exception {
        mvc.perform(multipart("/test/multipart"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/test/multipart"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void unsupportedMediaType_returns415() throws Exception {
        mvc.perform(post("/test/unsupported")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"x\":1}"))
                .andExpect(status().isUnsupportedMediaType())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(415))
                .andExpect(jsonPath("$.error").value("Unsupported Media Type"))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.path").value("/test/unsupported"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }

    @Test
    void genericException_returns500() throws Exception {
        mvc.perform(get("/test/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(500))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Unexpected error"))
                .andExpect(jsonPath("$.path").value("/test/generic"))
                .andExpect(jsonPath("$.fieldErrors").doesNotExist());
    }
}