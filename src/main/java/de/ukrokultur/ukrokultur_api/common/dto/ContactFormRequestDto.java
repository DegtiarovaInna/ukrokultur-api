package de.ukrokultur.ukrokultur_api.common.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ContactFormRequestDto(

        @NotBlank(message = "First name is required")
        @Size(max = 100, message = "First name must be <= 100 characters")
        String firstName,


        @NotBlank(message = "Last name is required")
        @Size(max = 100, message = "Last name must be <= 100 characters")
        String lastName,


        @NotBlank(message = "Email is required")
        @Size(max = 255, message = "Email must be <= 255 characters")
        @Pattern(
                regexp = "^$|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,63}$",
                message = "Email must be a valid ASCII email address"
        )
        String email,


        @NotBlank(message = "Phone is required")
        @Size(max = 50, message = "Phone must be <= 50 characters")
        String phone,


        @NotBlank(message = "Message is required")
        @Size(max = 5000, message = "Message must be <= 5000 characters")
        String message,


        @NotNull(message = "Privacy policy acceptance is required")
        @AssertTrue(message = "Privacy policy must be accepted")
        Boolean privacyPolicyAccepted,


        @NotBlank(message = "Privacy policy version is required")
        @Size(max = 50, message = "Privacy policy version must be <= 50 characters")
        String privacyPolicyVersion,

        @NotBlank(message = "hCaptcha token is required")
        @Size(max = 2000, message = "hCaptcha token must be <= 2000 characters")
        String hcaptchaToken

) {
}