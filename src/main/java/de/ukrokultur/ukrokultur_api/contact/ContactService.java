package de.ukrokultur.ukrokultur_api.contact;

import de.ukrokultur.ukrokultur_api.common.dto.ContactFormRequestDto;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@Service
public class ContactService {

    private final HCaptchaService hCaptchaService;
    private final ContactMailService mailService;

    public ContactService(HCaptchaService hCaptchaService, ContactMailService mailService) {
        this.hCaptchaService = hCaptchaService;
        this.mailService = mailService;
    }

    public void submit(ContactFormRequestDto req, String userAgent, String ip) {


        if (req.privacyPolicyAccepted() == null || !req.privacyPolicyAccepted()) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "privacyPolicyAccepted must be true");
        }


        if (!StringUtils.hasText(req.privacyPolicyVersion())) {
            throw new ApiException(400, ErrorCode.VALIDATION_ERROR, "privacyPolicyVersion is required");
        }


        if (!StringUtils.hasText(req.email())) {
            throw new ApiException(400, ErrorCode.EMAIL_INVALID, "Email is required");
        }


        boolean ok = hCaptchaService.verify(req.hcaptchaToken());
        if (!ok) {
            throw new ApiException(400, ErrorCode.CAPTCHA_INVALID, "hCaptcha verification failed");
        }

        OffsetDateTime acceptedAtUtc = OffsetDateTime.now(ZoneOffset.UTC);
        mailService.send(req, acceptedAtUtc, userAgent, ip);
    }
}
