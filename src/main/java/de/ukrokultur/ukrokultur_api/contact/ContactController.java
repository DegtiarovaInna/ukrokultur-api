package de.ukrokultur.ukrokultur_api.contact;

import de.ukrokultur.ukrokultur_api.common.dto.ContactFormRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;

@Tag(name = "Contact", description = "Public contact form endpoint.")
@RestController
@RequestMapping("/contact")
public class ContactController {

    private final ContactService contactService;

    public ContactController(ContactService contactService) {
        this.contactService = contactService;
    }

    @Operation(summary = "Submit contact form", description = "Validates input, verifies hCaptcha (if enabled) and sends an email via Resend.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK"),
            @ApiResponse(responseCode = "400", description = "Validation / captcha error", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Mail configuration error", content = @Content(schema = @Schema(implementation = de.ukrokultur.ukrokultur_api.common.error.ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<Void> submit(@RequestBody @Valid ContactFormRequestDto req,
                                       HttpServletRequest request) {

        String userAgent = request.getHeader("User-Agent");


        String ip = resolveClientIp(request);

        contactService.submit(req, userAgent, ip);
        return ResponseEntity.ok().build();
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
