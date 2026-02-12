package de.ukrokultur.ukrokultur_api.common.dto;

public record I18nText(
        @jakarta.validation.constraints.NotBlank String en,
        @jakarta.validation.constraints.NotBlank String de,
        @jakarta.validation.constraints.NotBlank String uk
) {}
