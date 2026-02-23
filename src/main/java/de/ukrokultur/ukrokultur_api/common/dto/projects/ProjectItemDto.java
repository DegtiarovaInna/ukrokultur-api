package de.ukrokultur.ukrokultur_api.common.dto.projects;

import de.ukrokultur.ukrokultur_api.common.dto.I18nText;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

public record ProjectItemDto(
        String id,
        String slug,

        I18nText title,
        I18nText subtitle,

        String coverImage,
        List<String> galleryImages,

        LocalDate startDate,
        LocalDate endDate,

        I18nText description,

        List<I18nText> goals,
        List<I18nText> activities,
        List<ProjectPartnerDto> partners,

        boolean published,
        int order,

        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {}
