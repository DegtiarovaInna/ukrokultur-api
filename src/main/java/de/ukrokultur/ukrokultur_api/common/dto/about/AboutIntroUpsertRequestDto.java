package de.ukrokultur.ukrokultur_api.common.dto.about;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record AboutIntroUpsertRequestDto(
        String image,
        @NotNull @Valid I18nText title,
        @NotNull @Valid I18nText text,
        @NotNull Boolean published
) {}
