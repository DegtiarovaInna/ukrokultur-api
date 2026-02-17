package de.ukrokultur.ukrokultur_api.common.dto.projects;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ProjectUpsertRequestDto(
        String slug,
        @NotNull I18nText title,
        @NotNull I18nText content,
        List<String> images,
        @NotNull Boolean published
) {}
