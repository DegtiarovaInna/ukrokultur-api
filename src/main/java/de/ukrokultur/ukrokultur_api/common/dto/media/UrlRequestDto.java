package de.ukrokultur.ukrokultur_api.common.dto.media;

import jakarta.validation.constraints.NotBlank;

public record UrlRequestDto(
        @NotBlank String url
) {}