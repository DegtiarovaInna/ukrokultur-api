package de.ukrokultur.ukrokultur_api.common.dto.about;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AboutMemberUpsertRequestDto(
       String slug,
        @NotBlank String name,
        String image,
        Integer order,
        @NotNull Boolean published,
        String instagramUrl,
        @NotNull I18nText role,
        @NotNull I18nText biography
) {}
