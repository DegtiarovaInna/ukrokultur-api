package de.ukrokultur.ukrokultur_api.contact;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
public class HCaptchaService {

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

        HCaptchaVerifyResponse resp = restClient.post()
                .uri("https://hcaptcha.com/siteverify")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(HCaptchaVerifyResponse.class);

        return resp != null && Boolean.TRUE.equals(resp.success());
    }

    public record HCaptchaVerifyResponse(Boolean success) {}
}
