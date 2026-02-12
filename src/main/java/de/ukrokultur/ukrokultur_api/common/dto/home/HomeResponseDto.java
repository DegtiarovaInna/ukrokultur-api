package de.ukrokultur.ukrokultur_api.common.dto.home;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;

import java.time.OffsetDateTime;
import java.util.List;

public record HomeResponseDto(
        HomeHeroDto hero,
        HomeMissionDto mission,
        HomeWorkFieldsDto workFields,
        OffsetDateTime updatedAt
) {
    public record HomeHeroDto(
            String image,
            I18nText title,
            I18nText subtitle,
            boolean published
    ) {}

    public record HomeMissionDto(
            String image,
            I18nText title,
            I18nText text,
            boolean published
    ) {}

    public record HomeWorkFieldsDto(
            boolean published,
            List<HomeWorkFieldItemDto> items
    ) {}

    public record HomeWorkFieldItemDto(
            String id,
            int order,
            boolean published,
            I18nText title,
            I18nText description
    ) {}
}
