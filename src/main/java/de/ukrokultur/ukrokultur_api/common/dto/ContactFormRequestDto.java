package de.ukrokultur.ukrokultur_api.common.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ContactFormRequestDto(
        @NotBlank
        @Size(max = 100)
        String firstName,
        @NotBlank
        @Size(max = 100)
        String lastName,
        @Email @NotBlank
        @Size(max = 255)
        String email,
        @NotBlank
        @Size(max = 50)
        String phone,
        @NotBlank
        @Size(max = 5000)
        String message,

        @NotNull Boolean privacyPolicyAccepted,
        @NotBlank
        @Size(max = 50)
        String privacyPolicyVersion,

        @NotBlank
        @Size(max = 2000)
        String hcaptchaToken
) {}
