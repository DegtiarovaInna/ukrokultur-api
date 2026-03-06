package de.ukrokultur.ukrokultur_api.contact;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class HCaptchaService {

    private static final Logger log = LoggerFactory.getLogger(HCaptchaService.class); // CHANGE: logging

    private final RestClient restClient;
    private final HCaptchaProperties props;

    public HCaptchaService(RestClient restClient, HCaptchaProperties props) {
        this.restClient = restClient;
        this.props = props;
    }

    public boolean verify(String token) {
        if (!props.enabled()) {
            return true;
        }
        if (props.secret() == null || props.secret().isBlank()) {
            return false;
        }
        if (token == null || token.isBlank()) {
            return false;
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", props.secret());
        form.add("response", token);

        try {
            HCaptchaVerifyResponse resp = restClient.post()
                    .uri("https://hcaptcha.com/siteverify")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(HCaptchaVerifyResponse.class);

            boolean ok = resp != null && Boolean.TRUE.equals(resp.success());
            if (!ok) {
                log.warn("hCaptcha verification failed (success=false or null response).");
            }

            return ok;

        } catch (Exception ex) {

            log.error("hCaptcha verification request failed.", ex);
            return false;
        }
    }

    public record HCaptchaVerifyResponse(Boolean success) {}
}