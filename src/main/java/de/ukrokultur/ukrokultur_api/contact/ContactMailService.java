package de.ukrokultur.ukrokultur_api.contact;

import de.ukrokultur.ukrokultur_api.common.dto.ContactFormRequestDto;
import de.ukrokultur.ukrokultur_api.common.error.ErrorCode;
import de.ukrokultur.ukrokultur_api.common.exception.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

@Service
public class ContactMailService {

    private static final Logger log = LoggerFactory.getLogger(ContactMailService.class);

    private final RestClient restClient;
    private final ContactMailProperties mailProps;
    private final ResendProperties resendProps;

    public ContactMailService(RestClient restClient, ContactMailProperties mailProps, ResendProperties resendProps) {
        this.restClient = restClient;
        this.mailProps = mailProps;
        this.resendProps = resendProps;
    }

    public void send(ContactFormRequestDto req, OffsetDateTime acceptedAtUtc, String userAgent, String ip) {
        if (!StringUtils.hasText(resendProps.apiKey())) {
            throw new ApiException(500, ErrorCode.INTERNAL_ERROR, "Resend is not configured (missing RESEND_API_KEY)");
        }
        if (!StringUtils.hasText(mailProps.to()) || !StringUtils.hasText(mailProps.from())) {
            throw new ApiException(500, ErrorCode.INTERNAL_ERROR, "Contact mail is not configured (missing CONTACT_MAIL_TO / RESEND_FROM)");
        }

        String subject = "Ukrokultur: contact form";
        String text = buildText(req, acceptedAtUtc, userAgent, ip);

        try {
            restClient.post()
                    .uri("https://api.resend.com/emails")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + resendProps.apiKey())
                    .body(Map.of(
                            "from", mailProps.from(),
                            "to", List.of(mailProps.to()),
                            "subject", subject,
                            "text", text
                    ))
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception ex) {
            log.error("Failed to send contact email via Resend. to={}, from={}, ip={}, userAgent={}",
                    mailProps.to(), mailProps.from(), ip, userAgent, ex);

            throw new ApiException(500, ErrorCode.INTERNAL_ERROR, "Failed to send email");
        }
    }

    private String buildText(ContactFormRequestDto req, OffsetDateTime acceptedAtUtc, String userAgent, String ip) {
        return """
                New contact form submission

                First name: %s
                Last name: %s
                Email: %s
                Phone: %s

                Message:
                %s

                Privacy:
                accepted: %s
                version: %s
                acceptedAt(UTC): %s
                ip: %s
                userAgent: %s
                """.formatted(
                req.firstName(),
                req.lastName(),
                req.email(),
                req.phone(),
                req.message(),
                req.privacyPolicyAccepted(),
                req.privacyPolicyVersion(),
                acceptedAtUtc,
                ip,
                userAgent
        );
    }
}