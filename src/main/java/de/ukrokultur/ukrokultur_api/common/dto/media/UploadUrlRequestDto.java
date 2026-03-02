package de.ukrokultur.ukrokultur_api.common.dto.media;

import jakarta.validation.constraints.NotBlank;

public record UploadUrlRequestDto(
        @NotBlank String fileName,
        @NotBlank String contentType
) {}
