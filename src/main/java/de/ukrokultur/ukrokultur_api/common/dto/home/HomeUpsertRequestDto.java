package de.ukrokultur.ukrokultur_api.common.dto.home;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record HomeUpsertRequestDto(
        @NotNull @Valid HomeHeroUpsertDto hero,
        @NotNull @Valid HomeMissionUpsertDto mission,
        @NotNull @Valid HomeWorkFieldsUpsertDto workFields
) {
    public record HomeHeroUpsertDto(
            String image,
            @NotNull @Valid I18nText title,
            @NotNull @Valid I18nText subtitle,
            @NotNull Boolean published
    ) {}

    public record HomeMissionUpsertDto(
            String image,
            @NotNull @Valid I18nText title,
            @NotNull @Valid I18nText text,
            @NotNull Boolean published
    ) {}

    public record HomeWorkFieldsUpsertDto(
            @NotNull Boolean published,
            @NotNull List<@Valid HomeWorkFieldItemUpsertDto> items
    ) {}

    public record HomeWorkFieldItemUpsertDto(
            @NotNull String id,
            @NotNull Integer order,
            @NotNull Boolean published,
            @NotNull @Valid I18nText title,
            @NotNull @Valid I18nText description
    ) {}
}
